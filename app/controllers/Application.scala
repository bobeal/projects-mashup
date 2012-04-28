package controllers

import play.api._
import play.api.libs.ws.WS
import play.api.mvc._
import models._
import models.ws.OAuth2Client

object Application extends Controller {
  
  def login = Action { implicit request =>
    Ok(views.html.login())
  }

  def registerWithGoogle = Action {
    val authorizationUrl = OAuth2Client.authorizationUrl
    Logger.debug("Redirecting to " + authorizationUrl)
    Redirect(authorizationUrl)
  }
  
  def googleCallback(error:String, code:String) = Action { request =>
    Logger.debug("got result : " + request.queryString)
    if (error != "") {
      Ok(views.html.login(error))
    } else {
      Async {
        Logger.debug("calling access token with " + OAuth2Client.accessTokenParameters(code))
        WS.url(OAuth2Client.TOKEN_URI)
            .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
            .post(OAuth2Client.accessTokenParameters(code)).map { response =>
          Logger.debug("got access token response : " + response.body)
          val accessToken = response.json \ "access_token"
          var user:User = WS.url(OAuth2Client.EMAIL_URI)
              .withHeaders("Authorization" -> "Bearer %s".format(accessToken))
              .get().map { response =>
            Logger.debug("got email response : " + response.body)
            val email = (response.json \ "email").as[String]
            var user = User.findByEmail(email) match {
              case Some(user) => user
              case None => {
                  val name = (response.json \ "name").as[String]
                  User.create(new User(name, email))
              }
            }
            user
          }.value.get

          Redirect("/").withSession(
              "accessToken" -> accessToken.toString(),
              "email" -> user.email
          )
        }
      }
    }
  }
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
}