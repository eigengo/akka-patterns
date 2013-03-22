package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain.User
import akka.util.Timeout
import spray.routing.HttpService
import akka.actor.ActorRef
import org.cakesolutions.akkapatterns.core.{NotRegisteredUser, RegisteredUser}


trait UserService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  import akka.pattern.ask
  implicit val timeout: Timeout
  def userActor: ActorRef

  // will return code 666 if NotRegisteredUser is received
  implicit val UserRegistrationErrorMarshaller = errorSelectingEitherMarshaller[NotRegisteredUser, RegisteredUser](666)

  val userRoute =
    path("user" / "register") {
      post {
          handleWith {user: User =>
            (userActor ? RegisteredUser(user)).mapTo[Either[NotRegisteredUser, RegisteredUser]]
          }
      }
    }

}
