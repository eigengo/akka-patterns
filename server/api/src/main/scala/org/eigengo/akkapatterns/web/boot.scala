package org.eigengo.akkapatterns.web

import org.eigengo.akkapatterns.core.ServerCore
import org.eigengo.akkapatterns.api.Api
import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props

trait Web extends SprayCanHttpServerApp {
  this: ServerCore =>

  override lazy val system = actorSystem

  val service = system.actorOf(Props[Api], "api")

  newHttpServer(service, name = "spray-http-server") ! Bind("0.0.0.0", 8080)

}
