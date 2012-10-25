package org.cakesolutions.akkapatterns.api

import java.net.InetAddress
import akka.actor.ActorSystem
import akka.pattern.ask
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.Directives
import org.cakesolutions.akkapatterns.core.application.{ PoisonPill, GetImplementation, Implementation }

case class SystemInfo(implementation: Implementation, host: String)

class HomeService(implicit val actorSystem: ActorSystem) extends Directives with Marshalling with MetaMarshallers with DefaultTimeout {

  def applicationActor = actorSystem.actorFor("/user/application")

  val route = {
    path(Slash) {
      get {
        complete {
          import scala.concurrent.ExecutionContext.Implicits._
          (applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName)
          }
        }
      }
    } ~
      path("poisonpill") {
        post {
          complete {
            applicationActor ! PoisonPill()

            "Goodbye"
          }
        }
      }
  }

}
