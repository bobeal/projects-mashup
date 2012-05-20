package models

import anorm._
import anorm.SqlParser._
import play.api.Play.current
import play.api.db.DB

case class Authorization(
  user: User,
  application: ApplicationType.Value,
  apiKey: String,
  userId: Option[String]
)

object Authorization {

  /**
   * Parse a Authorization from a ResultSet
   */
  val simple = {
    get[String]("authorizations.user_email") ~
    get[String]("authorizations.application") ~
    get[String]("authorizations.api_key") ~
    get[Option[String]]("authorizations.user_id") map {
      case user_email~application~api_key~user_id => 
        Authorization(User.findByEmail(user_email).get,ApplicationType.withName(application),api_key,user_id)
    }
  }

  def create(authorization: Authorization): Authorization = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into authorizations values (
          {user_email}, {application}, {api_key}, {user_id}
          )
        """).on(
          'user_email -> authorization.user.email,
          'application -> authorization.application.toString,
          'api_key -> authorization.apiKey,
          'user_id -> authorization.userId).executeUpdate()

      authorization
    }
  }
  
  def findByUser(user:User) : List[Authorization] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from authorizations where user_email = {user_email}
        """).on('user_email -> user.email).as(Authorization.simple *)
    }
  }

  def findByUserAndApplication(user:User, application:ApplicationType.Value) : Option[Authorization] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from authorizations where user_email = {user_email} and application = {application}
        """).on('user_email -> user.email,
                'application -> application.toString).as(Authorization.simple.singleOpt)
    }
  }

  def updateApiKey(authorization:Authorization, apiKey:String) : Authorization = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update authorizations set api_key = {api_key} where user_email = {user_email} and application = {application}
        """).on('api_key -> apiKey,
                'user_email -> authorization.user.email,
                'application -> authorization.application.toString).executeUpdate()
      
      authorization
    }
  }
}
