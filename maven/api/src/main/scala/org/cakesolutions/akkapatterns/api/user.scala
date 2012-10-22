package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import cc.spray.Directives
import org.cakesolutions.akkapatterns.domain.User
import org.cakesolutions.akkapatterns.core.application.{NotRegisteredUser, RegisteredUser}
import akka.pattern.ask

/**
 * @author janmachacek
 */
class UserService(implicit val actorSystem: ActorSystem) extends Directives with Marshallers with Unmarshallers with DefaultTimeout with LiftJSON {
  def userActor = actorSystem.actorFor("/user/application/user")

  val route =
    path("user" / "register") {
      post {
        content(as[User]) { user =>
          completeWith((userActor ? RegisteredUser(user)).mapTo[Either[NotRegisteredUser, RegisteredUser]])
        }
      }
    }

}
