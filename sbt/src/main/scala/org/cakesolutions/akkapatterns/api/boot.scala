package org.cakesolutions.akkapatterns.api

import akka.actor.{ActorRef, Props}
import cc.spray._
import http.{StatusCodes, HttpResponse}
import org.cakesolutions.akkapatterns.core.Core
import akka.util.Timeout

trait Api {
  this: Core =>

  val routes =
    new HomeService().route ::
    //new DummyService("customers").route ::
    new CustomerService().route ::
    Nil

  def rejectionHandler: PartialFunction[scala.List[cc.spray.Rejection], cc.spray.http.HttpResponse] = {
    case (rejections: List[Rejection]) => HttpResponse(StatusCodes.BadRequest)
  }

  val svc: Route => ActorRef = route => actorSystem.actorOf(Props(new HttpService(route, rejectionHandler)))

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