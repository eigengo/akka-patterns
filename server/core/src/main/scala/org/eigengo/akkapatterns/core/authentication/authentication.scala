package org.eigengo.akkapatterns.core.authentication

import org.eigengo.akkapatterns.domain._
import java.util.UUID
import akka.actor.{ActorRef, Props, Actor}

/**
 * Some failure in the login process
 */
trait LoginFailure extends ApplicationFailure

/**
 * ADT representing failures in the first phase of the login process.
 * {{{
 *   data FirstPhaseLoginFailure = BadUsernameOrPassword [ ... | AccountExpired | AccountLocked ]
 * }}}
 */
trait FirstPhaseLoginFailure extends LoginFailure
/**
 * The username or password combination was not valid
 */
case class BadUsernameOrPassword() extends FirstPhaseLoginFailure


/**
 * Begins the login process by using the supplied username, password and deviceId
 *
 * @param username the username
 * @param password the password
 * @param deviceId some digest of the client that's making the request. This will allow us to have support
 *                        "don't ask for the 2nd phase for the next 30 days on this device"
 */
case class FirstLogin(username: String, password: String, deviceId: Option[String])

/**
 * ADT for the login data
 * {{{
 *   data LoggedIn = LoggedInPartially UUID | LoggedInFully UUID
 * }}}
 */
trait LoggedIn

/**
 * If the first phase succeeds (i.e. account is there, active, ...), but needs 2nd phase authentication, we somehow
 * deliver a secret to the user and return the token that represents partially logged in user. This token is not valid
 * for anything else other than completing the authentication, that is, turning the partial token into the full one.
 *
 * @param token the partial token
 */
case class LoggedInPartially(token: UUID) extends LoggedIn

/**
 * Once the user has the token, he or she can complete the login by turning the partial ``token`` into the full one.
 *
 * @param token the partial token
 * @param secret the secret that the user received from the previous step
 */
case class SecondLogin(token: UUID, secret: String)

/**
 * Removes the ``token`` from the list of logged in tokens. Any further requests with the same ``token`` should be rejected
 *
 * @param token the token to remove
 */
case class Logout(token: UUID)

/**
 * ADT for the failures in the second phase of the login process
 * {{{
 *   data SecondPhaseFailure = BadPartialToken | TooManyBadSecrets
 * }}}
 *
 */
trait SecondPhaseLoginFailure extends LoginFailure

/**
 * The given secret did not match or the token was not there
 */
case class BadPartialToken() extends SecondPhaseLoginFailure

/**
 * Generated when the permitted number of attempts to login with a token has been reached
 */
case class TooManyBadSecrets() extends SecondPhaseLoginFailure


/**
 * The final success. The ``token`` represents a fully authenticated principal.
 *
 * @param token the token representing a user that is fully authenticated
 */
case class LoggedInFully(token: UUID) extends LoggedIn

/**
 * Builds the hierarchy of the login actors
 */
class AuthenticationActor(messageDelivery: ActorRef) extends Actor {
  val login = context.actorOf(Props(new LoginActor(messageDelivery)), "login")
  val account = context.actorOf(Props[AccountActor], "account")

  def receive = {
    case _ =>
  }
}
