package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

case class Project(
    id: Pk[Long], 
    name:String
)

object Project {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("project.id") ~
    get[String]("project.name") map {
      case id~name => Project(id,name)
    }
  }

  def create(project: Project): Project = {
    DB.withConnection { implicit connection =>
      val id: Long = project.id.getOrElse {
        SQL("select nextval('project_seq')").as(scalar[Long].single)
      }
      SQL(
        """
          insert into project values (
            {id}, {name}
          )
        """).on(
          'id -> id,
          'name -> project.name).executeUpdate()

      project.copy(id = Id(id))
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
