package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.routing.Directives
import java.net.InetAddress
import akka.pattern.ask
import org.cakesolutions.akkapatterns.core.application.{ PoisonPill, GetImplementation, Implementation }
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.MetaMarshallers

case class SystemInfo(implementation: Implementation, host: String)

class HomeService(implicit val actorSystem: ActorSystem) extends Directives with Marshalling with MetaMarshallers with DefaultTimeout {

  def applicationActor = actorSystem.actorFor("/user/application")

  val route = {
    path(Slash) {
      get {
        complete {
          import scala.concurrent.ExecutionContext.Implicits._
          val futureInfo = (applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName)
          }
          // how to get this to implicitly return as a future marshaller of a SystemInfo marshaller? 
          futureInfo
        }
      }
    } ~
      path("poisonpill") {
        post {
          completeWith {
            applicationActor ! PoisonPill()

            "Goodbye"
          }
        }
      }
  }

}
