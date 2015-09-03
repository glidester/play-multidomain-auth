package utils.silhouette

import models.{User, TokenUser}
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global

trait SilhouetteWebController extends SilhouetteController[User] {
	
	lazy val identityService = new UserService
	lazy val passwordInfoDAO = new PasswordInfoWebDAO
	lazy val tokenService = new TokenUserService
	
	implicit lazy val env = Environment[User, CookieAuthenticator](
		identityService,
		authenticatorService,
		Seq(credentialsProvider),
		eventBus
	)
}