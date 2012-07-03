package org.cakesolutions.akkapatterns.web

import akka.actor.Props
import cc.spray.can.server.HttpServer
import cc.spray.io.IoWorker
import cc.spray.io.pipelines.MessageHandlerDispatch
import org.cakesolutions.akkapatterns.core.Core
import org.cakesolutions.akkapatterns.api.Api
import cc.spray.SprayCanRootService

trait Web {
  this: Api with Core =>

  // every spray-can HttpServer (and HttpClient) needs an IoWorker for low-level network IO
  // (but several servers and/or clients can share one)
  val ioWorker = new IoWorker(actorSystem).start()

  // create and start the spray-can HttpServer, telling it that we want requests to be
  // handled by the root service actor
  val sprayCanServer = actorSystem.actorOf(
    Props(new HttpServer(ioWorker, MessageHandlerDispatch.SingletonHandler(
      actorSystem.actorOf(Props(new SprayCanRootService(rootService)))))),
    name = "http-server"
  )

  // a running HttpServer can be bound, unbound and rebound
  // initially to need to tell it where to bind to
  sprayCanServer ! HttpServer.Bind("0.0.0.0", 8080)

  // finally we drop the api thread but hook the shutdown of
  // our IoWorker into the shutdown of the applications ActorSystem
  actorSystem.registerOnTermination {
    ioWorker.stop()
  }

}