package utils.silhouette

import models.{TokenService, TokenManager}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class TokenManagerService extends TokenService[TokenManager] {
	def create (token: TokenManager): Future[Option[TokenManager]] = {
		TokenManager.save(token).map(Some(_))
	}
	def retrieve (id: String): Future[Option[TokenManager]] = {
		TokenManager.findById(id)
	}
	def consume (id: String): Unit = {
		TokenManager.delete(id)
	}
}