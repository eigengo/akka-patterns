package org.cakesolutions.akkapatterns.api

import spray.routing.{RequestContext, AuthenticationFailedRejection, AuthenticationRequiredRejection}
import concurrent.Future
import spray.routing.authentication.Authentication
import java.util.UUID
import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain._
import org.cakesolutions.akkapatterns.core.authentication.TokenCheck

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

  import concurrent.ExecutionContext.Implicits.global

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
      val header = ctx.request.headers.find(_.name == "x-token")
      if (header.isEmpty)
        Future(Left(AuthenticationRequiredRejection("https", "zoetic")))
      else doAuthenticate(UUID.fromString(header.get.value)).map {
        case Some(user) => map(user)
        case None       => Left(AuthenticationFailedRejection("Zoetic"))
      }
  }

  def validUser: RequestContext => Future[Authentication[UserDetail]] = doValidUser(x => Right(x.asInstanceOf[UserDetailT[UserKind]]))

  def validSuperuser: RequestContext => Future[Authentication[UserDetailT[SuperuserKind.type]]] = doValidUser(x => Right(new UserDetailT[SuperuserKind.type](x.userReference, SuperuserKind)))

  def validCustomer: RequestContext => Future[Authentication[UserDetailT[CustomerUserKind]]] = {
    doValidUser { udc: UserDetailT[_] =>
      udc.kind match {
        case k: CustomerUserKind => Right(new UserDetailT(udc.userReference, k))
        case _ => Left(AuthenticationFailedRejection("Akka-Patterns"))
      }
    }
  }
}

/**
 * provides a default implementation for Authentication Directives
 */
trait DefaultAuthenticationDirectives extends AuthenticationDirectives with DefaultTimeout {
  this: { def actorSystem: ActorSystem } =>

  import concurrent.ExecutionContext.Implicits.global
  import akka.pattern.ask

  def loginActor = actorSystem.actorFor("/user/application/authentication/login")

  override def doAuthenticate(token: UUID) = (loginActor ? TokenCheck(token)).mapTo[Option[UserDetailT[_]]]

}

