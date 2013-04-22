package org.eigengo.akkapatterns.web

import org.eigengo.akkapatterns.core.{LocalAmqpServerCore, ServerCore}
import org.eigengo.akkapatterns.api.Api
import spray.can.server.SprayCanHttpServerApp
import akka.actor.Props

trait Web extends SprayCanHttpServerApp {
  this: ServerCore with Api =>

  override lazy val system = actorSystem

  newHttpServer(rootService, name = "spray-http-server") ! Bind("0.0.0.0", 8080)

}
