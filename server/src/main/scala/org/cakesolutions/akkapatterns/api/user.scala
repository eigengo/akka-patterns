package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.Directives
import org.cakesolutions.akkapatterns.domain.{UserFormats, User}
import spray.httpx.SprayJsonSupport

/**
 * @author janmachacek
 */
class UserService(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout with UserFormats with MetaMarshallers with SprayJsonSupport {
  def userActor = actorSystem.actorFor("/user/application/user")

  val route =
    path("user" / "register") {
      post {
        entity(as[User]) { user =>
          complete {
            // (userActor ? RegisteredUser(user)).mapTo[Either[NotRegisteredUser, RegisteredUser]]
            "Wait a bit!"
          }
        }
      }
    }

}
