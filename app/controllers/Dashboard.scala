package controllers

import play.api.mvc.Controller
import models.User

object Dashboard extends Controller with Secured {

  def index = IsAuthenticated { username => _ => 
    User.findByEmail(username).map { user =>
      Ok(
        views.html.index("Welcome", user)
      )
    }.getOrElse(Redirect("/login"))
  }
}