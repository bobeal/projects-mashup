package controllers

import models.Users
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Controller
import models.Authorization
import models.ApplicationType

object Authorizations extends Controller with Secured with Users {

  def index = IsAuthenticated { username => implicit request =>
    val authorizations = Authorization.findByUser(user)
    val authorizationsByApplication = ApplicationType.values.map { applicationType => 
      (applicationType.toString(), authorizations.find { authorization =>
        applicationType.toString() == authorization.application.toString()
      })
    }
    Ok(views.html.authorization.index(authorizationsByApplication))
  }

  def add(application:String) = IsAuthenticated { username => implicit request =>
    Form("apiKey" -> nonEmptyText).bindFromRequest.fold(
      errors => BadRequest,
      apiKey => {
        Authorization.create(Authorization(user, ApplicationType.withName(application), apiKey))
        Redirect(routes.Authorizations.index)
      }
    )
  }
}
