package utils.silhouette

import com.mohiva.play.silhouette.api.util.{CacheLayer, PasswordInfo}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.repositories.DelegableAuthInfoRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.util.{PlayCacheLayer, SecureRandomIDGenerator, BCryptPasswordHasher}
import scala.concurrent.ExecutionContext.Implicits.global

trait SilhouetteController[I <: Identity] extends Silhouette[I, CookieAuthenticator] with AuthenticatorServiceModule {

	lazy val eventBus = EventBus()
	lazy val idGenerator = new SecureRandomIDGenerator
	lazy val passwordHasher = new BCryptPasswordHasher
	
	lazy val authInfoService = new DelegableAuthInfoRepository(passwordInfoDAO)
	lazy val credentialsProvider = new RequestCredentialsProvider(new CredentialsProvider(authInfoService, passwordHasher, Seq(passwordHasher)))
	lazy val providers = Map(credentialsProvider.id -> credentialsProvider)

	def identityService: IdentityService[I]
	def passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]
	//def tokenService: TokenService[User]
	
	def env: Environment[I, CookieAuthenticator]
}