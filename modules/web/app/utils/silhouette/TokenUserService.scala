package utils.silhouette

import models.{TokenService, TokenUser}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class TokenUserService extends TokenService[TokenUser] {
	def create (token: TokenUser): Future[Option[TokenUser]] = {
		TokenUser.save(token).map(Some(_))
	}
	def retrieve (id: String): Future[Option[TokenUser]] = {
		TokenUser.findById(id)
	}
	def consume (id: String): Unit = {
		TokenUser.delete(id)
	}
}