package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models.Users
import models.Project
import anorm.NotAssigned
import models.SourceType
import models.DataSource
import models.providers.GDocs
import play.api.Logger

object Projects extends Controller with Secured with Users {

  def index = IsAuthenticated { username => implicit request =>
      Ok(html.project.index(Project.list(user)))
  }

  def newProject = IsAuthenticated { username => implicit request =>
      Ok(html.project.create())
  }

  def create = IsAuthenticated { username => implicit request =>
      Form("name" -> nonEmptyText).bindFromRequest.fold(
          errors => BadRequest,
          name => {
            Project.create(Project(NotAssigned, name, user))
            Redirect(routes.Projects.index)
          }
      )
  }

  def configure(id:Long) = IsAuthenticated { username => implicit request =>
    Project.findById(id) match {
      case None => NotFound
      case p => {
        val projectDataSources = DataSource.findByProject(id)
        val availableDataSources = Map(SourceType.Gdocs_Collection.toString() -> GDocs.listSources(user))
        val sortedDataSources = SourceType.values.map { sourceType =>
          (sourceType.toString(), projectDataSources.filter { dataSource =>
            dataSource.sourceType.toString() == sourceType.toString()
          })
        }
        Ok(html.project.configure(p.get, sortedDataSources, availableDataSources))
      }
    }
  }
  
  def addGdocsCollection(id:Long) = IsAuthenticated { username => implicit request =>
    Form("gdocs_collection" -> nonEmptyText).bindFromRequest.fold(
        errors => BadRequest,
        gdocs_collection => {
          val dataSource = GDocs.getSource(user, gdocs_collection)
          DataSource.create(DataSource(dataSource.sourceType, dataSource.id, dataSource.url,
              dataSource.name, Project.findById(id).get))
        }
    )

    Redirect(routes.Projects.configure(id))
  }
}