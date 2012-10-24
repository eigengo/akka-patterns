package org.cakesolutions.akkapatterns.api

import akka.actor.{ActorRef, Props}
import spray._
import routing._
import http.{StatusCodes, HttpResponse}
import org.cakesolutions.akkapatterns.core.Core
import akka.util.Timeout

trait Api {
  this: Core =>

  val routes =
    new HomeService().route ::
    new CustomerService().route ::
    new UserService().route ::
    Nil

  def rejectionHandler: PartialFunction[scala.List[Rejection], HttpResponse] = {
    case (rejections: List[Rejection]) => HttpResponse(StatusCodes.BadRequest)
  }

  // FIXME: HttpService is now a trait
  // @see http://spray.io/documentation/spray-routing/key-concepts/big-picture/#the-httpservice
  // this file should probably be rewritten to use the new API
  val svc: Route => ActorRef = route =>
    actorSystem.actorOf(Props(new HttpService(route, rejectionHandler)))

  val rootService = actorSystem.actorOf(
    props = Props(new RootService(
      svc(routes.head),
      routes.tail.map(svc):_*
    )),
    name = "root-service"
  )

}

trait DefaultTimeout {
  final implicit val timeout = Timeout(3000)

}