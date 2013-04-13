package org.eigengo.akkapatterns.api

import spray.routing.{HttpService, RequestContext, AuthenticationFailedRejection, AuthenticationRequiredRejection}
import concurrent.Future
import spray.routing.authentication.Authentication
import java.util.UUID
import akka.actor.ActorRef
import org.eigengo.akkapatterns.domain._
import org.eigengo.akkapatterns.core.authentication.TokenCheck
import akka.util.Timeout
import spray.http.HttpRequest

/**
 * Mix in this trait to get the authentication directive. The ``validUser`` function can be used in Spray's
 * ``authentication`` function.
 *
 * The usual pattern is
 *
 * {{{
 * ... with AuthenticationDirectives {
 *
 *   ...
 *
 *   path("users/me/portfolio") {
 *     authenticate(validUser) { userDetail =>
 *     }
 *   }
 *   ...
 * }
 * }}}
 *
 * @author janmachacek
 */
trait AuthenticationDirectives {
  this: HttpService =>

  /**
   * @return a `User` that has been previously identified with the `Token` we have been given.
   */
  def doAuthenticate(token: UUID): Future[Option[UserDetailT[_]]]

  /**
   * @return the function that is usable in Spray's ``authenticate`` function, giving
   *         routes access to a ``UserDetail`` instance
   */
  private def doValidUser[A <: UserKind](map: UserDetailT[_] => Authentication[UserDetailT[A]]): RequestContext => Future[Authentication[UserDetailT[A]]] = {
    ctx: RequestContext =>
      getToken(ctx.request) match {
        case None => Future(Left(AuthenticationRequiredRejection("https", "patterns")))
        case Some(token) => doAuthenticate(token) .map {
          case Some(user) => map(user)
          case None       => Left(AuthenticationFailedRejection("Patterns"))
        }
      }
  }

  // http://en.wikipedia.org/wiki/Universally_unique_identifier
  val uuidRegex = """^\p{XDigit}{8}(-\p{XDigit}{4}){3}-\p{XDigit}{12}$""".r
  def isUuid(token: String) = token.length == 36 && uuidRegex.findPrefixOf(token).isDefined

  def getToken(request: HttpRequest): Option[UUID] = {
    val query = request.queryParams.get("token")
    if (query.isDefined && isUuid(query.get))
      Some(UUID.fromString(query.get))
    else {
      val header = request.headers.find(_.name == "x-token")
      if (header.isDefined && isUuid(header.get.value))
        Some(UUID.fromString(header.get.value))
      else
        None
    }
  }

  /**
   * Checks that the token represents a valid user; i.e. someone is logged in. We make no assumptions about the roles
   *
   * @return the authentication of any user kind
   */
  def validUser: RequestContext => Future[Authentication[UserDetail]] = doValidUser(x => Right(x.asInstanceOf[UserDetailT[UserKind]]))

  /**
   * Checks that the token represents a valid superuser
   *
   * @return the authentication for superuser
   */
  def validSuperuser: RequestContext => Future[Authentication[UserDetailT[SuperuserKind.type]]] =
    doValidUser { udc: UserDetailT[_] =>
      udc.kind match {
        case SuperuserKind => Right(new UserDetailT(udc.userReference, SuperuserKind))
        case _ => Left(AuthenticationFailedRejection("Akka-Patterns"))
      }
    }

  /**
   * Checks that the token represents a valid customer
   *
   * @return the authentication for superuser
   */
  def validCustomer: RequestContext => Future[Authentication[UserDetailT[CustomerUserKind]]] = {
    doValidUser { udc: UserDetailT[_] =>
      udc.kind match {
        case k: CustomerUserKind => Right(new UserDetailT(udc.userReference, k))
        case _ => Left(AuthenticationFailedRejection("Akka-Patterns"))
      }
    }
  }
}


trait DefaultAuthenticationDirectives extends AuthenticationDirectives {
  this: HttpService =>

  import akka.pattern.ask
  implicit val timeout: Timeout
  def loginActor: ActorRef

  override def doAuthenticate(token: UUID) = (loginActor ? TokenCheck(token)).mapTo[Option[UserDetailT[_]]]

}

