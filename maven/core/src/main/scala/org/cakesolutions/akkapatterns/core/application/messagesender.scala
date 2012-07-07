package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor

case class Message(address: String, body: String)

class MessageSenderActor extends Actor {
  protected def receive = {
    case Message(address, body) =>
      // do something
  }
}
