package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor

case class Bomb()

class BombActor extends Actor {

  protected def receive = {
    case Bomb() =>
      sender ! Some("boom!")

  }
}
