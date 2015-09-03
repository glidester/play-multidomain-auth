package utils.silhouette

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.Manager
import play.api.mvc.{Request, RequestHeader}
import play.api.i18n.{Messages, Lang}
import scala.concurrent.Future


/**
	Only allows those managers that have at least a role of the selected.
	Master role is always allowed.
	Ex: WithRole("high", "sales") => only managers with roles "high" or "sales" (or "master") are allowed.
*/
case class WithRole (anyOf: String*) extends Authorization[Manager,CookieAuthenticator] {
  override def isAuthorized[B](manager: Manager, authenticator: CookieAuthenticator)(implicit request: Request[B], messages: Messages): Future[Boolean] = Future.successful(WithRole.isAuthorized(manager, anyOf:_*))
}
object WithRole {
	def isAuthorized (manager: Manager, anyOf: String*): Boolean =
		anyOf.intersect(manager.roles).size > 0 || manager.roles.contains("master")
}

/**
	Only allows those managers that have every of the selected roles.
	Master role is always allowed.
	Ex: Restrict("high", "sales") => only managers with roles "high" and "sales" (or "master") are allowed.
*/
case class WithRoles (allOf: String*) extends Authorization[Manager,CookieAuthenticator] {
  override def isAuthorized[B] (manager: Manager, authenticator: CookieAuthenticator)(implicit request: Request[B], messages: Messages): Future[Boolean] = Future.successful(WithRoles.isAuthorized(manager, allOf:_*))
}
object WithRoles {
	def isAuthorized (manager: Manager, allOf: String*): Boolean =
		allOf.intersect(manager.roles).size == allOf.size || manager.roles.contains("master")
}
