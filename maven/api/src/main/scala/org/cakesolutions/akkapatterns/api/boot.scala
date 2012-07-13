package org.cakesolutions.akkapatterns.api

import akka.actor.{ActorRef, Props}
import cc.spray.{RootService, Route, HttpService, SprayCanRootService}
import org.cakesolutions.akkapatterns.core.Core
import akka.util.Timeout

trait Api {
  this: Core =>

  val routes =
    new HomeService().route :: Nil

  val svc: Route => ActorRef = route => actorSystem.actorOf(Props(new HttpService(route)))

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