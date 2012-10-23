package org.cakesolutions.akkapatterns.core

import akka.actor.{Props, ActorSystem}
import application.ApplicationActor
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Await

case class Start()
case class Started()

case class Stop()

trait Core {
  implicit def actorSystem: ActorSystem
  implicit val timeout = Timeout(30000)

  val application = actorSystem.actorOf(
    props = Props[ApplicationActor],
    name = "application"
  )

  Await.ready(application ? Start(), timeout.duration)

}
