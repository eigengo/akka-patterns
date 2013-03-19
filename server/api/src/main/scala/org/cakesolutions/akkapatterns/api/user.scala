package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain.User
import akka.util.Timeout
import spray.routing.HttpService
import akka.actor.ActorRef


trait UserService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  import akka.pattern.ask
  implicit val timeout: Timeout
  def userActor: ActorRef

  val userRoute =
    path("user" / "register") {
      post {
          handleWith {user: User =>
            // (userActor ? RegisteredUser(user)).mapTo[Either[NotRegisteredUser, RegisteredUser]]
            "Wait a bit!"
          }
      }
    }

}
