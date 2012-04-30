package controllers

import play.api.mvc.Controller
import models.User
import play.api.libs.ws.WS
import play.api.Logger
import models.Users
import models.Project
import models.DataSource
import models.providers.GDocs
import models.DataSourceEntry
import scala.collection.mutable.HashMap
import scala.collection.mutable.Set

object Dashboard extends Controller with Secured with Users {

  def index = IsAuthenticated { username => implicit request =>
    val entriesByProject = HashMap.empty[Project, Set[DataSourceEntry]]
    Project.list(user).map { project =>
      DataSource.findByProject(project.id.get).map { dataSource =>
        GDocs.getSourceEntries(user, dataSource.id).map { dataSourceEntry =>
          if (!entriesByProject.isDefinedAt(project))
            entriesByProject.put(project, Set.empty[DataSourceEntry])
          entriesByProject.get(project).get += dataSourceEntry
        }
      }
    }

    Ok(views.html.dashboard.index(entriesByProject))
  }
}