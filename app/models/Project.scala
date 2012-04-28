package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

case class Project(
    name:String
)

object Project {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("project.name") map {
      case name => Project(name)
    }
  }

  def create(project: Project): Project = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into project values (
            {name}
          )
        """).on(
          'name -> project.name).executeUpdate()

      project
    }
  }
  
  def list() : List[Project] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from project
        """).as(Project.simple *)
    }
  }
}
