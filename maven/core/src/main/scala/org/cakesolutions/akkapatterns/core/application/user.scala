package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import com.mongodb.casbah.MongoCollection
import org.cakesolutions.akkapatterns.domain
import domain.User
import com.mongodb.casbah.commons.MongoDBObject
import java.security.MessageDigest

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


trait UserOperations extends TypedCasbah with SearchExpressions {
  def users: MongoCollection
  def sha1 = MessageDigest.getInstance("SHA1")

  def getUser(id: domain.Identity) = users.findOne(entityId(id)).map(mapper[User])

  def getUserByUsername(username: String) = users.findOne(MongoDBObject("username" -> username)).map(mapper[User])

  def registerUser(user: User): Either[Failure, RegisteredUser] = {
    getUserByUsername(user.username) match {
      case None =>
        val hashedPassword = java.util.Arrays.toString(sha1.digest(user.password.getBytes))
        val userToRegister = user.copy(password = hashedPassword)
        users += serialize(userToRegister)
        Right(RegisteredUser(userToRegister))
      case Some(_existingUser) =>
        Left(NotRegisteredUser("User.duplicateUsername"))
    }
  }

}

class UserActor extends Actor with UserOperations with MongoCollections {

  protected def receive = {
    case Get(id) =>
      sender ! getUser(id)

    case GetUserByUsername(username) =>
      sender ! getUserByUsername(username)

    case RegisteredUser(user) =>
      sender ! registerUser(user)
  }
}
