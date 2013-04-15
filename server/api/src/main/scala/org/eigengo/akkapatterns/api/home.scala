package org.eigengo.akkapatterns.api

import java.net.InetAddress
import org.eigengo.akkapatterns.core.{GetImplementation, Implementation}
import java.util.Date
import spray.routing.HttpService
import akka.util.Timeout
import akka.actor.ActorRef

case class SystemInfo(host: String, timestamp: Date)

trait HomeService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  import akka.pattern.ask
  implicit val timeout: Timeout

  val homeRoute = {
    path(Slash) {
      get {
        complete {
          SystemInfo(InetAddress.getLocalHost.getCanonicalHostName, new Date)
        }
      }
    }
  }

}
