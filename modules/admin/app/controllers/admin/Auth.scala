package controllers.admin

import javax.inject.Inject

import com.mohiva.play.silhouette.impl.util.PlayCacheLayer
import play.api._
import play.api.cache.CacheApi
import play.api.mvc._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, Messages}
import utils.silhouette._
import utils.silhouette.Implicits._
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, LoginInfo}
import com.mohiva.play.silhouette.api.util.{CacheLayer, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.{AccessDeniedException}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class Auth @Inject() (cache: CacheLayer, messages: MessagesApi) extends SilhouetteAdminController {

  override def cacheLayer: CacheLayer = cache
  override def messagesApi: MessagesApi = messages

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
			case Some(manager) => Redirect(routes.Application.index)
			case None => Ok(views.html.admin.auth.signIn(signInForm))
		})
    }
	
	/**
	* Authenticates the manager based on his email and password
	*/
	def authenticate = Action.async { implicit request =>
		signInForm.bindFromRequest.fold(
			formWithErrors => Future.successful(BadRequest(views.html.admin.auth.signIn(formWithErrors))),
			credentials => {
				credentialsProvider.authenticate(request).flatMap { optLoginInfo =>
          val oo = optLoginInfo.map { loginInfo =>
            identityService.retrieve(loginInfo).flatMap {
              case Some(manager) => authenticatorService.create(loginInfo).flatMap { authenticator =>
                eventBus.publish(LoginEvent(manager, request, request2Messages))
                authenticatorService.init(authenticator)
                Future.successful(Redirect(routes.Application.index))
              }
              case None => Future.failed(new RuntimeException("Couldn't find manager"))
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
	* Signs out the manager
	*/
	def signOut = SecuredAction.async { implicit request =>
		eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
		authenticatorService.retrieve.flatMap {
			case Some(authenticator) => authenticatorService.discard(authenticator, Redirect(routes.Application.index))
			case None => Future.failed(new RuntimeException("Couldn't find authenticator"))
		}
	}
	
	/**
	* Shows an error page when the manager tries to get to an area without the necessary roles.
	*/
	def accessDenied = SecuredAction.async { implicit request =>
		Future.successful(Ok(views.html.admin.auth.accessDenied(request.identity, request)))
	}
	
}