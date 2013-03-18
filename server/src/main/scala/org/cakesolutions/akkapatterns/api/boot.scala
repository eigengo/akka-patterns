package org.cakesolutions.akkapatterns.api

import akka.actor.{ActorRef, Props}
import spray._
import routing._
import http.{StatusCodes, HttpResponse}
import org.cakesolutions.akkapatterns.core.ServerCore
import akka.util.Timeout

trait Api extends RouteConcatenation {
  this: ServerCore =>

  val routes =
    new HomeService().route ~
    new CustomerService().route

  def rejectionHandler: PartialFunction[scala.List[Rejection], HttpResponse] = {
    case (rejections: List[Rejection]) => HttpResponse(StatusCodes.BadRequest)
  }

  val rootService = actorSystem.actorOf(Props(new RoutedHttpService(routes)))

}

trait DefaultTimeout {
  final implicit val timeout = Timeout(3000)

}