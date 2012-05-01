package models.providers

import java.text.SimpleDateFormat

import play.api.libs.ws.WS
import play.api.Logger

import org.apache.commons.codec.binary.Base64

import models._

object Basecamp extends DataSourceProvider {

    // 2012-04-27T16:06:33.829Z
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  def listSources(user: User): List[DataSource] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Basecamp) match {
      case None => null
      case authorization => {
        val encodedToken = new String(Base64.encodeBase64((authorization.get.apiKey + ":x").getBytes()))
        WS.url("https://colcoz.basecamphq.com/projects.xml")
          .withHeaders("Accept" -> "application/xml",
                       "Content-Type" -> "application/xml",
                       "Authorization" -> "Basic %s".format(encodedToken))
          .get().map { response =>
            // Logger.debug("Basecamp response : " + response.body)
            (response.xml \ "project").toList.map { node =>
              val id = (node \ "id").head.text
              val title = (node \ "name").head.text
              val url = "https://colcoz.basecamphq.com/" + "projects/" + id

              DataSource(SourceType.Basecamp_Project, id, url, title, null)
            }
          }.value.get.sortBy { dataSource => dataSource.name }
      }
    }
  }
  
  def getSource(user:User, sourceId:String): DataSource = {
    Authorization.findByUserAndApplication(user, ApplicationType.Basecamp) match {
      case None => null
      case authorization => {
        val encodedToken = new String(Base64.encodeBase64((authorization.get.apiKey + ":x").getBytes()))
        WS.url("https://colcoz.basecamphq.com/projects/" + sourceId + ".xml")
          .withHeaders("Accept" -> "application/xml",
                       "Content-Type" -> "application/xml",
                       "Authorization" -> "Basic %s".format(encodedToken))
          .get().map { response =>
            // Logger.debug("got resource " + response.xml)
            val id = (response.xml \ "id").head.text
            val title = (response.xml \ "name").head.text
            val url = "https://colcoz.basecamphq.com/" + "projects/" + id

            DataSource(SourceType.Basecamp_Project, id, url, title, null)
          }.value.get
      }
    }
  }

  def getSourceEntries(user:User, sourceId:String) : List[DataSourceEntry] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Basecamp) match {
      case None => null
      case authorization => {
        val encodedToken = new String(Base64.encodeBase64((authorization.get.apiKey + ":x").getBytes()))
        WS.url("https://colcoz.basecamphq.com/projects/" + sourceId + "/posts.xml")
          .withHeaders("Accept" -> "application/xml",
                       "Content-Type" -> "application/xml",
                       "Authorization" -> "Basic %s".format(encodedToken))
          .get().map { response =>
            // Logger.debug("got resource " + response.xml)
            (response.xml \ "post").toList.map { node =>
              val label = (node \ "title").head.text
              val url = (node \ "id").head.text
              var modificationDate = (node \ "commented-at").head.text match {
                case "" => (node \ "posted-on").head.text
                case s => s
              }

              DataSourceEntry(SourceType.Basecamp_Project, DataSource.findById(sourceId).get.name, label, url, dateFormatter.parse(modificationDate))
            }
          }.value.get
      }
    }
  }
}