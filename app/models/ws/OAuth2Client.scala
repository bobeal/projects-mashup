package models.ws

import java.net.URLEncoder

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS

object OAuth2Client {

  val PLAY_CONF = Play.current.configuration
  val SCOPE = "https://www.googleapis.com/auth/calendar+https://www.google.com/m8/feeds/+https://docs.google.com/feeds/+https://mail.google.com/mail/feed/atom+https://spreadsheets.google.com/feeds/+https://www.googleapis.com/auth/userinfo.profile+https://www.googleapis.com/auth/userinfo.email"
  val REDIRECT_URI = "http://localhost:9000/register/google/callback"
  val AUTHORIZATION_URI = "https://accounts.google.com/o/oauth2/auth"
  val TOKEN_URI = "https://accounts.google.com/o/oauth2/token"

  val EMAIL_URI = "https://www.googleapis.com/oauth2/v2/userinfo#email"
  val PROFILE_URI = "https://www.googleapis.com/oauth2/v2/userinfo"

  val authorizationUrl = "%s?scope=%s&redirect_uri=%s&client_id=%s&response_type=code&approval_prompt=auto"
    .format(AUTHORIZATION_URI, SCOPE, REDIRECT_URI, PLAY_CONF.getString("google.client.id").get)

  def accessTokenParameters(code:String) : String = {
    val accessTokenParameters = "code=%s&redirect_uri=%s&client_id=%s&client_secret=%s&grant_type=authorization_code"
      .format(URLEncoder.encode(code, "UTF-8"), URLEncoder.encode(REDIRECT_URI, "UTF-8"), 
          PLAY_CONF.getString("google.client.id").get, 
          URLEncoder.encode(PLAY_CONF.getString("google.client.secret").get, "UTF-8"))
    accessTokenParameters
  }
}
