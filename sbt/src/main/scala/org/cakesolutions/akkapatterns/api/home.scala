package org.cakesolutions.akkapatterns.api

import java.net.InetAddress
import akka.actor.ActorSystem
import akka.pattern.ask
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.Directives
import org.cakesolutions.akkapatterns.core.application.{ PoisonPill, GetImplementation, Implementation }
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits._
import spray.routing.directives.{MarshallingDirectives, CompletionMagnet}

case class SystemInfo(implementation: Implementation, host: String, timestamp: Long)

class HomeService(implicit val actorSystem: ActorSystem) extends Directives with Marshalling with MetaMarshallers with DefaultTimeout {

  def applicationActor = actorSystem.actorFor("/user/application")

  val route = {
    path(Slash) {
      get {
        handleWith { x: Any =>
          (applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName, new Date().getTime)
          }
        }
      }
    } /*~
      path("poisonpill") {
        post {
          complete {
            applicationActor ! PoisonPill()

            "Goodbye"
          }
        }
      }*/
  }

}
