package org.eigengo.akkapatterns.api

import akka.actor.{Props, ActorRef, Actor}
import spray._
import http.{StatusCodes, HttpResponse}
import routing._
import org.eigengo.akkapatterns.core.ServerCore
import akka.util.Timeout

/*


class XApi(loginActor: ActorRef, recogCoordinator: ActorRef) extends Actor with HttpServiceActor
  with FailureHandling
  with Tracking with Configured
  with EndpointMarshalling
  with DefaultAuthenticationDirectives
  with CustomerService
  with HomeService
  with UserService
  with RecogService {

  // used by the Akka ask pattern
  implicit val timeout = Timeout(10000)

  val routes =
      customerRoute ~
      homeRoute ~
      userRoute ~
      recogRoute

  def receive = runRoute (
    handleRejections(rejectionHandler)(
      handleExceptions(exceptionHandler)(
        trackRequestResponse(routes)
      )
    )
  )

}
*/

trait Api extends RouteConcatenation {
  this: ServerCore =>

  implicit val executionContext = actorSystem.dispatcher

  val routes =
    new RecogService(recogCoordinator, actorSystem.settings.config.getString("server.web-server")).route

  val rootService = actorSystem.actorOf(Props(new RoutedHttpService(routes)))

}
