package controllers.web

import utils.silhouette._
import scala.concurrent.Future

object Application extends SilhouetteWebController {

	def index = UserAwareAction.async { implicit request =>
		Future.successful(Ok(views.html.web.index(request.identity)))
	}
	
	def myAccount = SecuredAction.async { implicit request =>
		Future.successful(Ok(views.html.web.myAccount(request.identity)))
	}

}