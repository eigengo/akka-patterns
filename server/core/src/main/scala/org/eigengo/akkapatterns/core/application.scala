package org.eigengo.akkapatterns.core

import akka.actor.{ActorLogging, Props, Actor}
import akka.routing.FromConfig

@deprecated("This is a rather clumsy idea.")
case class GetImplementation()
@deprecated("This is a rather clumsy idea.")
case class Implementation(title: String, version: String, build: String)

@deprecated("This is a rather clumsy idea.")
object ApplicationActor {
  case class Start()
  case class Stop()
}

@deprecated("This is a rather clumsy idea.")
class ApplicationActor extends Actor with ActorLogging {
  import ApplicationActor._

  def receive = {
    case GetImplementation() =>
      val title = "Akka-Patterns"
      val version = "1.0"
      val build = "1.0"

      sender ! Implementation(title, version, build)

    case Start() =>
      val messageDelivery = context.actorOf(
        Props[MessageDeliveryActor].withRouter(FromConfig()).withDispatcher("low-priority-dispatcher"),
        "messageDelivery"
      )
      context.actorOf(Props(new UserActor(messageDelivery)).withRouter(FromConfig()), "user")


    case Stop() =>
      context.children.foreach(context.stop _)

  }
}
