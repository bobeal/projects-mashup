package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

object SourceType extends Enumeration("Gdocs_Collection", "Gmail_Label", "Basecamp_Project") {
  type SourceType = Value
  val Gdocs_Collection, Gmail_Label, Basecamp_Project = Value
}

case class DataSource(
  sourceType: SourceType.Value,
  id: String,
  url: String,
  name: String,
  project: Project
)

object DataSource {

  /**
   * Parse a DataSource from a ResultSet
   */
  val simple = {
    get[String]("data_source.source_type") ~
    get[String]("data_source.id") ~
    get[String]("data_source.url") ~
    get[String]("data_source.name") ~
    get[Long]("data_source.project_id") map {
      case source_type~id~url~name~project_id => DataSource(SourceType.withName(source_type),id,url,name,Project.findById(project_id).get)
    }
  }

  def create(dataSource: DataSource): DataSource = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into data_source values (
          {source_type}, {id}, {url}, {name}, {project_id}
          )
        """).on(
          'source_type -> dataSource.sourceType.toString(),
          'id -> dataSource.id,
          'url -> dataSource.url,
          'name -> dataSource.name,
          'project_id -> dataSource.project.id).executeUpdate()

      dataSource
    }
  }
  
  def findByProject(projectId:Long) : List[DataSource] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from data_source where project_id = {project_id}
        """).on('project_id -> projectId).as(DataSource.simple *)
    }
  }

  def findById(id:String) : Option[DataSource] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from data_source where id = {id}
        """).on('id -> id).as(DataSource.simple.singleOpt)
    }
  }
}

trait DataSourceProvider {
  def listSources(user: User): List[DataSource]
  def getSource(user:User, sourceId:String): DataSource
  def getSourceEntries(user:User, sourceId:String) : List[DataSourceEntry]
}

