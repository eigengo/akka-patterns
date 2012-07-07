package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout

case class Notify(person: String)

class NotificationActor extends Actor {
  implicit val timeout = Timeout(1000)
  def messageSender = context.actorFor("/user/application/messageSender")
  def addressBook = context.actorFor("/user/application/addressBook")

  protected def receive = {
    case Notify(person) =>
      val addrs = (addressBook ? GetAddresses(person)).mapTo[List[String]]
      // do some work to construct the body and then
      val body = "Dummy body"

      // notify all
      addrs.foreach(_.foreach(messageSender ! Message(_, body)))
  }
}
