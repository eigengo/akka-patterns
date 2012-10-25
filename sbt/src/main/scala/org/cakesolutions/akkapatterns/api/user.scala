package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.routing.Directives
import akka.pattern.ask
import spray.httpx.marshalling.MetaMarshallers
import spray.httpx.SprayJsonSupport._
import org.cakesolutions.akkapatterns.domain.User
import org.cakesolutions.akkapatterns.core.application.{ NotRegisteredUser, RegisteredUser }

/**
 * @author janmachacek
 */
class UserService(implicit val actorSystem: ActorSystem) extends Directives with DefaultTimeout with Marshalling with MetaMarshallers {
  def userActor = actorSystem.actorFor("/user/application/user")

  val route =
    path("user" / "register") {
      post {
        entity(as[User]) { user =>
          complete {
            import scala.concurrent.ExecutionContext.Implicits._
            (userActor ? RegisteredUser(user)).mapTo[Either[NotRegisteredUser, RegisteredUser]]
          }
        }
      }
    }

}
