package utils.silhouette

import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.Manager
import Implicits._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class PasswordInfoAdminDAO extends DelegableAuthInfoDAO[PasswordInfo] {

	def save (loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
		Manager.findByEmail(loginInfo).map { maybeManager =>
			maybeManager.map { manager =>
				Manager.save(manager.copy(password = authInfo))
			}
			authInfo
		}

	def find (loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
		Manager.findByEmailMap(loginInfo) { manager => manager.password }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}