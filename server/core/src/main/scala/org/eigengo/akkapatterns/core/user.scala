package org.eigengo.akkapatterns.core

import akka.actor.{ActorRef, Actor}
import org.eigengo.akkapatterns.domain
import domain.{UserFormats, ApplicationFailure, User}
import org.neo4j.graphdb.GraphDatabaseService

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
case class NotRegisteredUser(code: String) extends ApplicationFailure

/**
 * Contains indexes for the ``User`` instances
 */
trait UserGraphDatabaseIndexes {
  this: GraphDatabase =>

  lazy val userIndex = graphDatabase.index().forNodes("user")

  implicit object UserIndexSource extends IndexSource[User] {
    def getIndex(graphDatabase: GraphDatabaseService) = userIndex
  }

}

/**
 * Contains the operations that manipulate the ``User`` nodes in Neo4j
 */
trait UserOperations extends TypedGraphDatabase with UserGraphDatabaseIndexes with SprayJsonNodeMarshalling with UserFormats {

  def getUserByUsername(username: String): Option[User] = findOneEntityWithIndex[User] { _.get("username", username) }

}

class UserActor(messageDelivery: ActorRef) extends Actor with UserOperations {

  def receive = {
    /* Replies with Option[User] */
    case GetUserByUsername(username) =>
      sender ! getUserByUsername(username)
  }
}
