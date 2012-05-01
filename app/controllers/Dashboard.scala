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
import scala.collection.mutable.Seq
import models.providers.Basecamp
import scala.collection.SortedSet

object Dashboard extends Controller with Secured with Users {

  def index = IsAuthenticated { username => implicit request =>
    val entriesByProject = HashMap.empty[Project, Seq[DataSourceEntry]]
    Project.list(user).map { project =>
      DataSource.findByProject(project.id.get).map { dataSource =>
        val dataSourceEntries = dataSource.sourceType.toString match {
          case "Gdocs_Collection" => GDocs.getSourceEntries(user, dataSource.id)
          case "Basecamp_Project" => Basecamp.getSourceEntries(user, dataSource.id)
        }
        dataSourceEntries.map { dataSourceEntry =>
          if (!entriesByProject.isDefinedAt(project))
            entriesByProject.put(project, Seq.empty[DataSourceEntry])
          entriesByProject(project) = (dataSourceEntry +: entriesByProject.get(project).get).sorted
        }
      }
      
    }

    Ok(views.html.dashboard.index(entriesByProject))
  }
}