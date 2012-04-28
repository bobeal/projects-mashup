package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.mvc.Session

case class User(email: String, name: String)

object User {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("users.email") ~
    get[String]("users.name") map {
      case email~name => User(email, name)
    }
  }

  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into users values (
            {email}, {name}
          )
        """).on(
          'email -> user.email,
          'name -> user.name).executeUpdate()

      user

    }
  }

  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from users where email = {email}").on(
        'email -> email).as(User.simple.singleOpt)
    }
  }
}

trait Users {
  implicit def user(implicit session: Session): User = {
    User.findByEmail(session.get("email").get).get
  }
}
