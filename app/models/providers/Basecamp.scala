package models.providers

import java.text.SimpleDateFormat

import play.api.libs.ws.WS
import play.api.Logger

import org.apache.commons.codec.binary.Base64

import models._

object Basecamp extends Application with DataSourceProvider {

    // 2012-04-27T16:06:33.829Z
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

  def userId(user: User): Option[String] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Basecamp) match {
      case None => null
      case authorization => {
        val encodedToken = new String(Base64.encodeBase64((authorization.get.apiKey + ":x").getBytes()))
        WS.url("https://colcoz.basecamphq.com/me.xml")
          .withHeaders("Accept" -> "application/xml",
                       "Content-Type" -> "application/xml",
                       "Authorization" -> "Basic %s".format(encodedToken))
          .get().map { response =>
            Logger.debug("Basecamp response : " + response.body)
            (response.xml \ "id").head.text
          }.value.get match {
            case "" => None
            case s => Some(s)
          }
      }
    }
  }

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
  
  def getSourceTaskEntries(user:User, sourceId:String) : List[DataSourceTask] = {
    Authorization.findByUserAndApplication(user, ApplicationType.Basecamp) match {
      case None => null
      case authorization => {
        val encodedToken = new String(Base64.encodeBase64((authorization.get.apiKey + ":x").getBytes()))
        val uncompletedTodoLists: List[(String,String)] = WS.url("https://colcoz.basecamphq.com/projects/" + sourceId + "/todo_lists.xml?filter=pending")
          .withHeaders("Accept" -> "application/xml",
                       "Content-Type" -> "application/xml",
                       "Authorization" -> "Basic %s".format(encodedToken))
          .get().map { response =>
            // Logger.debug("got todo lists : " + response.xml)
            (response.xml \ "todo-list").toList.map { node =>
              val todoListName = (node \ "name").head.text
              val todoListId = (node \ "id").head.text
              todoListId -> todoListName
            }
          }.value.get
        uncompletedTodoLists.map { todoListEntry =>
            WS.url("https://colcoz.basecamphq.com/todo_lists/" + todoListEntry._1 + "/todo_items.xml")
                .withHeaders("Accept" -> "application/xml",
                             "Content-Type" -> "application/xml",
                             "Authorization" -> "Basic %s".format(encodedToken))
                .get().map { response =>
                    Logger.debug("got todo list items : " + response.xml)
                    (response.xml \ "todo-item").toList.filter { node =>
                        (node \ "completed").head.text == "false" &&
                            ((node \ "responsible-party-id").headOption match {
                          case None => false
                          case Some(node) => node.text == authorization.get.userId.get
                        })
                    }.map { node =>
                        val id = (node \ "id").head.text
                        var url = "https://colcoz.basecamphq.com/todo_items/" + id + ".xml"
                        val label = (node \ "content").head.text + "(" + todoListEntry._2 + ")"
                        val dueDate = (node \ "due-at").head.text match {
                          case "" => new java.util.Date()
                          case s => dateFormatter.parse((node \ "due-at").head.text)
                        }
                
                        DataSourceTask(SourceType.Basecamp_Project, DataSource.findById(sourceId).get.name, label, url, dueDate)
                    }
                }.value.get
          }.flatten
      }
    }
  }

}