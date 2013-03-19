package org.cakesolutions.akkapatterns.api

import java.net.InetAddress
import org.cakesolutions.akkapatterns.core.{GetImplementation, Implementation}
import java.util.Date
import spray.routing.HttpService
import akka.util.Timeout
import akka.actor.ActorRef

case class SystemInfo(implementation: Implementation, host: String, timestamp: Date)

trait HomeService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  import akka.pattern.ask
  implicit val timeout: Timeout
  def applicationActor: ActorRef

  val homeRoute = {
    path(Slash) {
      get {
        complete {
          (applicationActor ? GetImplementation()).mapTo[Implementation].map {
            SystemInfo(_, InetAddress.getLocalHost.getCanonicalHostName, new Date)
          }
        }
      }
    }
  }

}
