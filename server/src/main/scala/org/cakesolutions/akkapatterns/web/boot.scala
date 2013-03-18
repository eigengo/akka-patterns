package org.cakesolutions.akkapatterns.web

import org.cakesolutions.akkapatterns.core.ServerCore
import org.cakesolutions.akkapatterns.api.Api
import spray.io.{SingletonHandler, IOBridge}
import spray.can.server.HttpServer
import akka.actor.Props
import org.cakesolutions.akkapatterns.HttpIO

trait Web extends HttpIO {
  this: Api with ServerCore =>

  // every spray-can HttpServer (and HttpClient) needs an IOBridge for low-level network IO
  // (but several servers and/or clients can share one)
  // val ioBridge = new IOBridge(actorSystem).start()

  // create and start the spray-can HttpServer, telling it that
  // we want requests to be handled by our singleton service actor
  val httpServer = actorSystem.actorOf(
    Props(new HttpServer(SingletonHandler(rootService))),
    name = "http-server"
  )

  // a running HttpServer can be bound, unbound and rebound
  // initially to need to tell it where to bind to
  httpServer ! HttpServer.Bind("localhost", 8080)

  // finally we drop the main thread but hook the shutdown of
  // our IOBridge into the shutdown of the applications ActorSystem
  actorSystem.registerOnTermination {
    // ioBridge ! Stop
  }

}
