package org.cakesolutions.akkapatterns.api

import java.net.InetAddress
import akka.actor.ActorSystem
import akka.pattern.ask
import spray.httpx.marshalling.MetaMarshallers
import spray.routing.Directives
import org.cakesolutions.akkapatterns.core.{ GetImplementation, Implementation }
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

trait HomerServiceMarshalling extends DefaultJsonProtocol {

  implicit val ImplementationFormat = jsonFormat3(Implementation)
  implicit val SystemInfoFormat = jsonFormat3(SystemInfo)

}

case class SystemInfo(implementation: Implementation, host: String, timestamp: Long)

class HomeService(implicit val actorSystem: ActorSystem) extends Directives with HomerServiceMarshalling with MetaMarshallers with SprayJsonSupport with DefaultTimeout {

  def applicationActor = actorSystem.actorFor("/user/application")

  val route = {
    path(Slash) {
      get {
        complete {
          val f =(applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName, new Date().getTime)
          }

          f
        }
      }
    }
  }

}
