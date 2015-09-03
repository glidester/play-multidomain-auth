package controllers.admin

import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.CacheLayer
import com.mohiva.play.silhouette.impl.util.PlayCacheLayer
import play.api._
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import models._
import utils.admin.Mailer
import utils.silhouette._
import scala.concurrent.Future

class Application @Inject() (cache: CacheLayer, messages: MessagesApi) extends SilhouetteAdminController {

	override def cacheLayer: CacheLayer = cache
	override def messagesApi: MessagesApi = messages

	def index = SecuredAction.async { implicit request =>
		Future.successful(Ok(views.html.admin.index(request.identity)))
	}
	def social = SecuredAction(WithRole("social")).async { implicit request =>
		Future.successful(Ok(views.html.admin.social(request.identity)))
	}
	def salesOrHigh = SecuredAction(WithRole("sales", "high")).async { implicit request =>
		Future.successful(Ok(views.html.admin.salesOrHigh(request.identity)))
	}
	def salesAndHigh = SecuredAction(WithRoles("sales", "high")).async { implicit request =>
		Future.successful(Ok(views.html.admin.salesAndHigh(request.identity)))
	}
	def admin = SecuredAction(WithRole("master")).async { implicit request =>
		Future.successful(Ok(views.html.admin.admin(request.identity)))
	}

}