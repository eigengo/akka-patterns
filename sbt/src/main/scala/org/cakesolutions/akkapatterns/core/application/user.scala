package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import org.cakesolutions.akkapatterns.domain
import domain.User

/**
 * Finds a user by the given username
 *
 * @param username the username
 */
case class GetUserByUsername(username: String)

/**
 * Registers a user. Checks the password complexity and that the username is not duplicate
 *
 * @param user the user to be registered
 */
case class Register(user: User)

/**
 * Successfully registered a user
 *
 * @param user the user that's just been registered
 */
case class RegisteredUser(user: User)

/**
 * Unsuccessful registration with the error code
 * @param code the error code
 */
case class NotRegisteredUser(code: String) extends Failure


trait UserOperations  {
  //def users: MongoCollection
  //def sha1 = MessageDigest.getInstance("SHA1")

  def getUser(id: domain.Identity): Option[User] = None

  def getUserByUsername(username: String): Option[User] = None

  def registerUser(user: User): Either[Failure, RegisteredUser] = {
    Left(NotRegisteredUser("User.notThereYet"))
  }

}

class UserActor extends Actor with UserOperations {

  def receive = {
    case Get(id) =>
      sender ! getUser(id)

    case GetUserByUsername(username) =>
      sender ! getUserByUsername(username)

    case RegisteredUser(user) =>
      sender ! registerUser(user)
  }
}
