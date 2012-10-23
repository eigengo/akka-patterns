package org.cakesolutions.akkapatterns.main

import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain.Configuration
import org.cakesolutions.akkapatterns.core.Core

object Main extends App {

  implicit val system = ActorSystem("AkkaPatterns")

  class Application(val actorSystem: ActorSystem) extends Core with Configuration {
  }

  new Application(system)

  sys.addShutdownHook {
    system.shutdown()
  }

}
