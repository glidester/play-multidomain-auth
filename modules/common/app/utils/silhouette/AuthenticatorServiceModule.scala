package utils.silhouette

import com.mohiva.play.silhouette.impl.util.DefaultFingerprintGenerator
import play.api.Play
import play.api.Play.current
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.api.util.{CacheLayer, IDGenerator, Clock}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

trait AuthenticatorServiceModule {

	lazy val authenticatorService: CookieAuthenticatorService = new CookieAuthenticatorService(
		CookieAuthenticatorSettings(
			cookieName = Play.configuration.getString("silhouette.authenticator.cookieName").get,
			cookiePath = Play.configuration.getString("silhouette.authenticator.cookiePath").get,
			cookieDomain = Play.configuration.getString("silhouette.authenticator.cookieDomain"),
			secureCookie = Play.configuration.getBoolean("silhouette.authenticator.secureCookie").get,
			httpOnlyCookie = Play.configuration.getBoolean("silhouette.authenticator.httpOnlyCookie").get,
			authenticatorIdleTimeout = Play.configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout").map(timeout => FiniteDuration(timeout,"minutes")),
			cookieMaxAge = Play.configuration.getInt("silhouette.authenticator.cookieMaxAge").map(timeout => FiniteDuration(timeout,"minutes")),
			authenticatorExpiry = FiniteDuration(Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get,"minutes")
		),
		None,
		new DefaultFingerprintGenerator(false),
		idGenerator,
		Clock()
	)



	def cacheLayer: CacheLayer
	def idGenerator: IDGenerator
}