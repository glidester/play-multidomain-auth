package utils.silhouette

import com.mohiva.play.silhouette.api.{LoginInfo, RequestProvider}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider._
import play.api.mvc.Request

import scala.concurrent.Future

class RequestCredentialsProvider(credentialsProvider: CredentialsProvider) extends RequestProvider {

  override def id = ID

  override def authenticate[B](request: Request[B]): Future[Option[LoginInfo]] = {

    //TODO: implement extraction of username and password to create LoginInfo
    //Future.successful(None)
    Future.successful(Some(LoginInfo(credentialsProvider.id,"master@myweb.com")))
  }
}
