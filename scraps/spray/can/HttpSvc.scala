package spray.can

import akka.actor._
import server.HttpServer
import spray.http._
import HttpMethods._
import spray.io.{IOBridge, SingletonHandler}

/**
 * Test service actor does something rather funky
 */
class TestService extends Actor {

  def receive = {
    case HttpRequest(GET, "/", _, _, _) =>
      sender ! HttpResponse(entity = "PONG!")

  }
}

object Main extends App {
  // we need an ActorSystem to host our application in
  val system = ActorSystem("simple-http-server")

  // every spray-can HttpServer (and HttpClient) needs an IOBridge for low-level network IO
  // (but several servers and/or clients can share one)
  val ioBridge = new IOBridge(system).start()

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[TestService])

  // create and start the spray-can HttpServer, telling it that we want requests to be
  // handled by our singleton handler
  val server = system.actorOf(
    props = Props(new HttpServerWS(ioBridge, SingletonHandler(handler))),
    name = "http-server"
  )

  // a running HttpServer can be bound, unbound and rebound
  // initially to need to tell it where to bind to
  server ! HttpServer.Bind("localhost", 8080)

  // finally we drop the main thread but hook the shutdown of
  // our IOBridge into the shutdown of the applications ActorSystem
  system.registerOnTermination {
    ioBridge.stop()
  }

}