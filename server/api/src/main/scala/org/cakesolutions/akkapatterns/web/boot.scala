package org.cakesolutions.akkapatterns.web

import org.cakesolutions.akkapatterns.core.ServerCore
import org.cakesolutions.akkapatterns.api.Api
import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props

trait Web extends SprayCanHttpServerApp {
  this: ServerCore =>

  val service = system.actorOf(Props[Api], "api")

  newHttpServer(service, name = "spray-http-server") ! Bind("0.0.0.0", 8080)

}
