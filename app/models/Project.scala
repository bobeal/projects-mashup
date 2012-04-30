package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

case class Project(
    id: Pk[Long], 
    name: String,
    user: User
)

object Project {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("project.id") ~
    get[String]("project.name") ~
    get[String]("project.user_email") map {
      case id~name~user_email => Project(id, name, User.findByEmail(user_email).get)
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
            {id}, {name}, {user_email}
          )
        """).on(
          'id -> id,
          'name -> project.name,
          'user_email -> project.user.email).executeUpdate()

      project.copy(id = Id(id))
    }
  }
  
  def list(user:User) : List[Project] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from project where user_email = {user_email}
        """).on('user_email -> user.email).as(Project.simple *)
    }
  }

  def findById(id:Long) : Option[Project] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from project where id = {id}
        """).on('id -> id).as(Project.simple.singleOpt)
    }
  }
}
