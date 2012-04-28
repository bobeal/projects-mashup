package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models.Users
import models.Project

object Projects extends Controller with Secured with Users {

  val projectForm = Form(
    mapping(
      "name" -> nonEmptyText)(Project.apply)(Project.unapply))

  def index = IsAuthenticated { username => implicit request =>
      Ok(html.project.index(Project.list()))
  }

  def newProject = IsAuthenticated { username => implicit request =>
      Ok(html.project.create())
  }

  def create = IsAuthenticated { username => implicit request =>
      projectForm.bindFromRequest.fold(
          errors => BadRequest,
          project => {
            Project.create(project)
            Redirect(routes.Projects.index)
          }
      )
  }
}