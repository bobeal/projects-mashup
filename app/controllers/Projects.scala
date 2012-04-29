package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models.Users
import models.Project
import anorm.NotAssigned

object Projects extends Controller with Secured with Users {

  def index = IsAuthenticated { username => implicit request =>
      Ok(html.project.index(Project.list()))
  }

  def newProject = IsAuthenticated { username => implicit request =>
      Ok(html.project.create())
  }

  def create = IsAuthenticated { username => implicit request =>
      Form("name" -> nonEmptyText).bindFromRequest.fold(
          errors => BadRequest,
          name => {
            Project.create(Project(NotAssigned, name))
            Redirect(routes.Projects.index)
          }
      )
  }
}