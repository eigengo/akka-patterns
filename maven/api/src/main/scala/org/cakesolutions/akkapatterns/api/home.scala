package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import cc.spray.Directives
import cc.spray.directives.Slash
import java.net.InetAddress
import akka.pattern.ask
import akka.util.Timeout
import org.cakesolutions.akkapatterns.core.application.{PoisonPill, GetImplementation, Implementation}

case class SystemInfo(implementation: Implementation, host: String)

class HomeService(implicit val actorSystem: ActorSystem) extends Directives with Marshallers with DefaultTimeout {

  def applicationActor = actorSystem.actorFor("/user/application")

  val route = {
    path(Slash) {
      get {
        completeWith {
          (applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName)
          }
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
