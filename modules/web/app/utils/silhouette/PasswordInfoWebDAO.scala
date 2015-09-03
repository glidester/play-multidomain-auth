package utils.silhouette

import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.User
import Implicits._
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class PasswordInfoWebDAO extends DelegableAuthInfoDAO[PasswordInfo] {

	def save (loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
		User.findByEmail(loginInfo).map { maybeUser =>
			maybeUser.map { user =>
				User.save(user.copy(password = authInfo))
			}
			authInfo
		}

	def find (loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
		User.findByEmail(loginInfo).map {
			case Some(user) if user.emailConfirmed => Some(user.password)
			case _ => None
		}

	override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

	override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

	override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}