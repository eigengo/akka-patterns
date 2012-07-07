package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor

case class GetAddresses(person: String)

class AddressBookActor extends Actor {
  protected def receive = {
    case GetAddresses(person) =>
      sender ! List(person + "@cakesolutions.net", person + "@gmail.com", person + "@hotmail.com")
  }
}
