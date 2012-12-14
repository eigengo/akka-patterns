package org.cakesolutions.akkapatterns.core.authentication

import akka.actor.{ ActorRef, Actor }
import java.util.{ Date, UUID }
import collection.mutable.ArrayBuffer
import scalaz.effect.IO
import org.cakesolutions.akkapatterns.core._
import org.cakesolutions.akkapatterns.core.DeliveryAddress
import scala.Some
import org.cakesolutions.akkapatterns.domain.{UserFormats, UserDetailT, User}
import org.cakesolutions.akkapatterns.core.DeliverSecret
import org.neo4j.graphdb.GraphDatabaseService

/**
 * Item in the token store that associates a particular ``token`` with the information about the user it belongs to,
 * expiry date, whether or not it is a partial token---if so, the secret must be supplied.
 *
 * @param userRef reference to an existing user
 * @param token the token
 * @param expires the expiry date (presumably in the future, though we don't check it here)
 * @param partial whether the token is a partial one
 * @param secret the secret associated with the partial token
 */
case class AuthenticationToken(userRef: UUID, token: UUID, expires: Date, partial: Boolean, retries: Int, secret: Option[String]) {

  /**
   * Decides whether the token is valid with respect to the given secret.
   *
   * @param s the given secret
   * @return ``true`` if the token is valid
   */
  def isValid(s: String) = secret == Some(s)
}

/**
 * Checks that the received ``token`` exists &amp; is valid
 *
 * @param token the token to check
 */
case class TokenCheck(token: UUID)

/**
 * Component that generates authentication tokens
 */
private[authentication] trait AuthenticationTokenGenerator {

  def generateAuthenticationToken(userRef: UUID): IO[AuthenticationToken] =
    IO(AuthenticationToken(userRef, UUID.randomUUID(), new Date(), false, 0, None))

  def generateSecret(token: AuthenticationToken): IO[AuthenticationToken] =
    IO(token.copy(
      retries = 2,
      secret = Some(UUID.randomUUID().toString.substring(0, 5)),
      partial = true))

}

/**
 * Contains functions that operate on the token keystore
 */
private[authentication] trait TokenOperations {
  val tokens = ArrayBuffer[AuthenticationToken]()

  /**
   * Creates a new token in the DB
   * @param token the token
   * @return the saved token
   */
  def create(token: AuthenticationToken): AuthenticationToken = {
    tokens += token
    token
  }

  /**
   * Finds existing [[org.cakesolutions.akkapatterns.core.authentication.AuthenticationToken]]
   * whose ``token`` field matches the parameter
   * @param token the token to find
   * @return Some(AuthencationToken) whose token matches the parameter
   */
  def find(token: UUID): Option[AuthenticationToken] = tokens.find(_.token == token)

  /**
   * Remove the [[org.cakesolutions.akkapatterns.core.authentication.AuthenticationToken]] whose ``token`` field matches
   * the parameter
   *
   * @param token the token to remove
   */
  def deleteByToken(token: UUID) {
    val i = tokens.indexWhere(_.token == token)
    if (i > -1) tokens.remove(i)
  }

  /**
   * Updates the ``token`` in the token store
   *
   * @param token the token to update
   * @return the updated token
   */
  def update(token: AuthenticationToken): AuthenticationToken = {
    deleteByToken(token.token)
    create(token)
  }
}

/**
 * Contains indexes for the ``Customer`` instances
 */
trait UserGraphDatabaseIndexes {
  this: GraphDatabase =>

  lazy val userIndex = graphDatabase.index().forNodes("user")

  implicit object UserIndexSource extends IndexSource[User] {
    def getIndex(graphDatabase: GraphDatabaseService) = userIndex
  }

}

/**
 * Login actor that supervises the actors in the login process.
 */
class LoginActor(messageDelivery: ActorRef) extends Actor with AuthenticationTokenGenerator with TokenOperations with TypedGraphDatabase
  with UserGraphDatabaseIndexes with SprayJsonNodeMarshalling with UserFormats {
  import scalaz.syntax.monad._

  /**
   * Saves the token in some persistent store
   *
   * @param at the token to be saved
   * @return the IO of the saved token
   */
  def createToken(at: AuthenticationToken): IO[AuthenticationToken] = IO(create(at))

  /**
   * Sends the secret to the user
   *
   * @param at the authentication token
   * @return the IO of the token
   */
  def deliverSecret(user: User)(at: AuthenticationToken): IO[AuthenticationToken] = {
    val deliveryAddress = DeliveryAddress(None, Some(user.lastName))

    messageDelivery ! DeliverSecret(deliveryAddress, at.secret.get)

    IO(at)
  }

  def continueLogin(user: User, deviceId: Option[String]): IO[Unit] = {
    deviceId.map(user.isTrustedDevice) match {
      case Some(true) =>
        generateAuthenticationToken(user.id) >>=
          createToken >>= {
          at => IO(sender ! Right(LoggedInFully(at.token)))
        }
      case _ =>
        generateAuthenticationToken(user.id) >>=
          generateSecret >>=
          createToken >>=
          deliverSecret(user) >>= {
          at => IO(sender ! Right(LoggedInPartially(at.token)))
        }
    }
  }

  def receive = {
    case FirstLogin(username, password, deviceId) =>
      findOneEntityWithIndex[User](_.get("username", username)) match {
        case Some(user) if (user.checkPassword(password)) =>
          continueLogin(user, deviceId).unsafePerformIO()
        case _ =>
          sender ! Left(BadUsernameOrPassword())
      }
    case SecondLogin(token, secret) =>
      find(token) match {
        case None =>
          sender ! Left(BadPartialToken())
        case Some(at) if !at.isValid(secret) && at.retries == 0 =>
          // no more retries
          deleteByToken(at.token)
          sender ! Left(TooManyBadSecrets())
        case Some(at) if !at.isValid(secret) && at.retries > 0 =>
          // bad secret, but retries still allowed
          update(at.copy(retries = at.retries - 1))
          sender ! Left(BadPartialToken())
        case Some(at) if at.isValid(secret) =>
          // delete the old one
          deleteByToken(at.token)

          val action = generateAuthenticationToken(at.userRef) >>=
            createToken >>=
            { at => IO(sender ! Right(LoggedInFully(at.token))) }

          action.unsafePerformIO()
      }
    case Logout(token) =>
      deleteByToken(token)

    case TokenCheck(token) =>
      val response =
      for {
        foundToken <- find(token)
        user       <- findOneEntity[User](foundToken.userRef)
        detail     =  UserDetailT(foundToken.userRef, user.kind)
      } yield detail
      sender ! response

  }

}
