package org.cakesolutions.akkapatterns.core

import akka.actor.{Props, Actor}

case class GetImplementation()
case class Implementation(title: String, version: String, build: String)

class ApplicationActor extends Actor {

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

      new SanityChecks {
        sender ! (if (ensureSanity) Started() else InmatesAreRunningTheAsylum)
      }

    /*
     * Stops this actor and all the child actors.
     */
    case Stop() =>
      context.children.foreach(context.stop _)

  }

}
