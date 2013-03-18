package org.cakesolutions.akkapatterns.core

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Await

case class Start()

case object InmatesAreRunningTheAsylum
case class Started()

case class Stop()

trait ServerCore {
  implicit def actorSystem: ActorSystem
  implicit val timeout = Timeout(30000)

  val application = actorSystem.actorOf(
    props = Props[ApplicationActor],
    name = "application"
  )

  Await.ready(application ? Start(), timeout.duration)

}
