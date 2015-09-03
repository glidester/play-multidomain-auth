package controllers.web

import javax.inject.Inject

import play.api._
import play.api.cache.CacheApi
import play.api.mvc._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.{MessagesApi, Messages}
import models._
import utils.silhouette._
import utils.silhouette.Implicits._
import com.mohiva.play.silhouette.api.{SignUpEvent, LoginEvent, LogoutEvent}
import com.mohiva.play.silhouette.api.util.{CacheLayer, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.{AccessDeniedException}
import utils.Constraints._
import utils.web.Mailer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

class Auth @Inject()(cache: CacheLayer, messages: MessagesApi) extends SilhouetteWebController {

  override def cacheLayer: CacheLayer = cache
  override def messagesApi: MessagesApi = messages

	// SIGN UP
	
	val signUpForm = Form(
		mapping(
			"id" -> ignored(None: Option[Long]),
			"email" -> email.verifying(maxLength(250), userUnique),
			"emailConfirmed" -> ignored(false),
			"password" -> nonEmptyText.verifying(minLength(6)),
			"nick" -> nonEmptyText,
			"firstName" -> nonEmptyText,
			"lastName" -> nonEmptyText
		)(User.apply)(User.unapply)
	)
	
	/**
	* Starts the sign up mechanism. It shows a form that the user have to fill in and submit.
	*/
    def startSignUp = UserAwareAction.async { implicit request =>
		Future.successful( request.identity match {
			case Some(user) => Redirect(routes.Application.index)
			case None => Ok(views.html.web.auth.signUp(signUpForm))
		})
    }
	

	/**
	* Handles the form filled by the user. The user and its password are saved and it sends him an email with a link to confirm his email address.
	*/
	def handleStartSignUp = Action.async { implicit request =>
		signUpForm.bindFromRequest.fold(
			formWithErrors => Future.successful(BadRequest(views.html.web.auth.signUp(formWithErrors))),
			user => {
				User.save(user)
				val authInfo = passwordHasher.hash(user.password)
				authInfoService.save(user.email, authInfo)
				val token = TokenUser(user.email, isSignUp = true)
				tokenService.create(token)
				Mailer.welcome(user, link = routes.Auth.signUp(token.id).absoluteURL())
				Future.successful(Ok(views.html.web.auth.almostSignedUp(user)))
			}
		)
	}
	
	/**
	* Confirms the user's email address based on the token and authenticates him.
	*/
    def signUp (tokenId: String) = Action.async { implicit request =>
		tokenService.retrieve(tokenId).flatMap {
			case Some(token) if (token.isSignUp && !token.isExpired) => {
				User.findByEmail(token.email).flatMap {
					case Some(user) => {
						authenticatorService.create(user.loginInfo).flatMap { authenticator =>
							if (!user.emailConfirmed) {
								User.save(user.copy(emailConfirmed = true))
								eventBus.publish(SignUpEvent(user, request, request2Messages))
							}
							eventBus.publish(LoginEvent(user, request, request2Messages))
							tokenService.consume(tokenId)
							authenticatorService.init(authenticator)

							Future.successful(Ok(views.html.web.auth.signedUp(user)))
						}
					}
					case None => Future.failed(new RuntimeException("Couldn't find user"))
				}
			}
			case Some(token) => {
				tokenService.consume(tokenId)
				notFoundDefault
			}
			case None => notFoundDefault
		}
    }
	
	
	// SIGN IN
	
	val signInForm = Form(
		mapping(
			"identifier" -> email,
			"password" -> nonEmptyText
		)(Credentials.apply)(Credentials.unapply)
	)
	
	/**
	* Starts the sign in mechanism. It shows the login form.
	*/
    def signIn = UserAwareAction.async { implicit request =>
		Future.successful( request.identity match {
			case Some(user) => Redirect(routes.Application.index)
			case None => Ok(views.html.web.auth.signIn(signInForm))
		})
    }
	
	/**
	* Authenticates the user based on his email and password
	*/
	def authenticate = Action.async { implicit request =>
		signInForm.bindFromRequest.fold(
			formWithErrors => Future.successful(BadRequest(views.html.web.auth.signIn(formWithErrors))),
			credentials => {
				credentialsProvider.authenticate(request).flatMap { optLoginInfo =>
          val oo = optLoginInfo.map { loginInfo =>
            identityService.retrieve(loginInfo).flatMap {
              case Some(user) => authenticatorService.create(loginInfo).flatMap { authenticator =>
                eventBus.publish(LoginEvent(user, request, request2Messages))
                authenticatorService.init(authenticator)
                Future.successful(Redirect(routes.Application.index))
              }
              case None => Future.failed(new RuntimeException("Couldn't find user"))
            }
          }

          oo.getOrElse( Future.failed(new RuntimeException("Couldn't find loginInfo")) )
				}.recoverWith {
					case e: AccessDeniedException => Future.successful(Redirect(routes.Auth.signIn).flashing("error" -> Messages("access.credentials.incorrect")))
				}.recoverWith(exceptionHandler)
			}
		)
	}
	
	
	// SIGN OUT
	
	/**
	* Signs out the user
	*/
	def signOut = SecuredAction.async { implicit request =>
		eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
		authenticatorService.retrieve.flatMap {
			case Some(authenticator) => authenticatorService.discard(authenticator, Redirect(routes.Application.index))
			case None => Future.failed(new RuntimeException("Couldn't find authenticator"))
		}
	}
	
	
	// FORGOT PASSWORD
	
	val emailForm = Form(single("email" -> email.verifying(userExists)))
	
	/**
	* Starts the reset password mechanism if the user has forgot his password. It shows a form to insert his email address.
	*/
    def forgotPassword = UserAwareAction.async { implicit request =>
		Future.successful( request.identity match {
			case Some(user) => Redirect(routes.Application.index)
			case None => Ok(views.html.web.auth.forgotPassword(emailForm))
		})
    }
	
	/**
	* Sends an email to the user with a link to reset the password
	*/
	def handleForgotPassword = Action.async { implicit request =>
		emailForm.bindFromRequest.fold(
			formWithErrors => Future.successful(BadRequest(views.html.web.auth.forgotPassword(formWithErrors))),
			email => {
				val token = TokenUser(email, isSignUp = false)
				tokenService.create(token)
				Mailer.forgotPassword(email, link = routes.Auth.resetPassword(token.id).absoluteURL())
				Future.successful(Ok(views.html.web.auth.forgotPasswordSent(email)))
			}
		)
	}
	
	val passwordsForm = Form(tuple(
		"password1" -> nonEmptyText(minLength = 6),
		"password2" -> nonEmptyText
	) verifying(Messages("passwords.not.equal"), passwords => passwords._2 == passwords._1 ))
	
	
	/**
	* Confirms the user's link based on the token and shows him a form to reset the password
	*/
    def resetPassword (tokenId: String) = Action.async { implicit request =>
		tokenService.retrieve(tokenId).flatMap {
			case Some(token) if (!token.isSignUp && !token.isExpired) => {
				Future.successful(Ok(views.html.web.auth.resetPassword(tokenId, passwordsForm)))
			}
			case Some(token) => {
				tokenService.consume(tokenId)
				notFoundDefault
			}
			case None => notFoundDefault
		}
	}
	
	/**
	* Saves the new password and authenticates the user
	*/
	def handleResetPassword (tokenId: String) = Action.async { implicit request =>
		passwordsForm.bindFromRequest.fold(
			formWithErrors => Future.successful(BadRequest(views.html.web.auth.resetPassword(tokenId, formWithErrors))),
			passwords => {
				tokenService.retrieve(tokenId).flatMap {
					case Some(token) if (!token.isSignUp && !token.isExpired) => {
						User.findByEmail(token.email).flatMap {
							case Some(user) => {
								val authInfo = passwordHasher.hash(passwords._1)
								authInfoService.save(token.email, authInfo)
								authenticatorService.create(user.loginInfo).flatMap { authenticator =>
									eventBus.publish(LoginEvent(user, request, request2Messages))
									tokenService.consume(tokenId)
									authenticatorService.init(authenticator)
									Future.successful(Ok(views.html.web.auth.resetedPassword(user)))
								}
							}
							case None => Future.failed(new RuntimeException("Couldn't find user"))
						}
					}
					case Some(token) => {
						tokenService.consume(tokenId)
						notFoundDefault
					}
					case None => notFoundDefault
				}
			}
		)
	}
	
	
	def notFoundDefault (implicit request: RequestHeader) =
		Future.successful(NotFound(views.html.web.errors.onHandlerNotFound(request)))
}