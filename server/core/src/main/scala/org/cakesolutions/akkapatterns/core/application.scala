package org.cakesolutions.akkapatterns.core

import akka.actor.{ActorLogging, Props, Actor}

case class GetImplementation()
case class Implementation(title: String, version: String, build: String)

object ApplicationActor {
  case class Start()
  case class Stop()
}

class ApplicationActor extends Actor with ActorLogging {
  import ApplicationActor._

  def receive = {
    case GetImplementation() =>
      val title = "Akka-Patterns"
      val version = "1.0"
      val build = "1.0"

      sender ! Implementation(title, version, build)

    case Start() =>
      val messageDelivery = context.actorOf(Props[MessageDeliveryActor], "messageDelivery")
      context.actorOf(Props(new CustomerActor(messageDelivery)), "customer")
      context.actorOf(Props(new UserActor(messageDelivery)),     "user")


    case Stop() =>
      context.children.foreach(context.stop _)

  }
}
