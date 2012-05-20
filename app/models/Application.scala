package models

object ApplicationType extends Enumeration("Google", "Basecamp") {
  type ApplicationType = Value
  var Google, Basecamp = Value
}

trait Application {
  def userId(user: User): Option[String]
}
