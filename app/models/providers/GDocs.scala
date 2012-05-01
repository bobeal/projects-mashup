package models.providers

import models.DataSourceProvider
import models.DataSource
import models.User
import play.api.libs.ws.WS
import models.Authorization
import models.ApplicationType
import play.api.Logger
import models.SourceType
import models.DataSourceEntry
import java.text.SimpleDateFormat
import java.util.Date

object GDocs extends DataSourceProvider {

  // 2012-04-27T16:06:33.829Z
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  def listSources(user: User): List[DataSource] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Google) match {
      case None => null
      case authorization => {
        WS.url("https://docs.google.com/feeds/default/private/full/-/folder")
          .withHeaders("Authorization" -> "Bearer %s".format(authorization.get.apiKey),
                       "GData-Version" -> "3.0")
          .withQueryString("showfolders" -> "true")
          .get().map { response =>
            // Logger.debug("gdocsCollection number " + response.xml)
            (response.xml \ "entry").toList.map { node =>
              val id = (node \ "resourceId").head.text
              val title = (node \ "title").head.text
              val url = (node \ "id").head.text
              val parent = (node \ "link").find { linkNode =>
                (linkNode \ "@rel").text == "http://schemas.google.com/docs/2007#parent"
              } match {
                case None => "(root)"
                case node => node.get.attribute("title").get.head.text
              }

              DataSource(SourceType.Gdocs_Collection, id, url, parent + "/" + title, null)
            }
          }.value.get.sortBy { dataSource => dataSource.name }
      }
    }
  }
  
  def getSource(user:User, sourceId:String): DataSource = {
    Authorization.findByUserAndApplication(user, ApplicationType.Google) match {
      case None => null
      case authorization => {
        WS.url("https://docs.google.com/feeds/default/private/full/" + sourceId)
          .withHeaders("Authorization" -> "Bearer %s".format(authorization.get.apiKey),
                       "GData-Version" -> "3.0")
          .withQueryString("showfolders" -> "true")
          .get().map { response =>
            // Logger.debug("got resource " + response.xml)
            val id = (response.xml \ "resourceId").head.text
            val title = (response.xml \ "title").head.text
            val url = (response.xml \ "id").head.text
            val parent = (response.xml \ "link").find { linkNode =>
              (linkNode \ "@rel").text == "http://schemas.google.com/docs/2007#parent"
            } match {
              case None => "(root)"
              case node => node.get.attribute("title").get.head.text
            }

            DataSource(SourceType.Gdocs_Collection, id, url, parent + "/" + title, null)
          }.value.get
      }
    }
  }

  def getSourceEntries(user:User, sourceId:String) : List[DataSourceEntry] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Google) match {
      case None => null
      case authorization => {
        WS.url("https://docs.google.com/feeds/default/private/full/" + sourceId + "/contents")
          .withHeaders("Authorization" -> "Bearer %s".format(authorization.get.apiKey),
                       "GData-Version" -> "3.0")
          .get().map { response =>
            // Logger.debug("got resource " + response.xml)
            (response.xml \ "entry").toList.map { node =>
              val label = (node \ "title").head.text
              val url = (node \ "id").head.text
              var modificationDate = (node \ "updated").head.text
              DataSourceEntry(SourceType.Gdocs_Collection, DataSource.findById(sourceId).get.name, label, url, dateFormatter.parse(modificationDate))
            }
          }.value.get
      }
    }
    
  }

}