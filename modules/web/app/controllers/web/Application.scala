package controllers.web

import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.CacheLayer
import play.api.i18n.MessagesApi
import utils.silhouette._
import scala.concurrent.Future

class Application @Inject() (cache: CacheLayer, messages: MessagesApi) extends SilhouetteWebController {

	override def cacheLayer: CacheLayer = cache
	override def messagesApi: MessagesApi = messages

	def index = UserAwareAction.async { implicit request =>
		Future.successful(Ok(views.html.web.index(request.identity)))
	}
	
	def myAccount = SecuredAction.async { implicit request =>
		Future.successful(Ok(views.html.web.myAccount(request.identity)))
	}
}