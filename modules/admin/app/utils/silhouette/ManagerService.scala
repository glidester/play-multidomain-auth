package utils.silhouette

import com.mohiva.play.silhouette.api.services.IdentityService
import models.Manager
import Implicits._
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class ManagerService extends IdentityService[Manager] {
	def retrieve (loginInfo: LoginInfo): Future[Option[Manager]] = Manager.findByEmail(loginInfo.providerKey)
}