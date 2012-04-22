package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class User(name: String, email: String)

object User {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.name") ~
    get[String]("user.email") map {
      case name~email => User(name, email)
    }
  }

  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into user values (
            {name}, {email}
          )
        """).on(
          'email -> user.email,
          'name -> user.name).executeUpdate()

      user

    }
  }

  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on(
        'email -> email).as(User.simple.singleOpt)
    }
  }
}
