package utils.silhouette

import models.{Manager, TokenManager}
import com.mohiva.play.silhouette.api.Environment
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import scala.concurrent.ExecutionContext.Implicits.global

trait SilhouetteAdminController extends SilhouetteController[Manager] {
	
	lazy val identityService = new ManagerService
	lazy val passwordInfoDAO = new PasswordInfoAdminDAO
	lazy val tokenService = new TokenManagerService

	implicit lazy val env = Environment[Manager, CookieAuthenticator](
		identityService,
		authenticatorService,
		Seq(credentialsProvider),
		eventBus
	)
}