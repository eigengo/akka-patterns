package org.cakesolutions.akkapatterns.main

import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain.Configuration
import org.cakesolutions.akkapatterns.core.Core
import org.cakesolutions.akkapatterns.api.Api
import org.cakesolutions.akkapatterns.web.Web

object Main extends App {

  implicit val system = ActorSystem("AkkaPatterns")

  class Application(val actorSystem: ActorSystem) extends Core with Api with Web with Configuration {
  }

  new Application(system)

  sys.addShutdownHook {
    system.shutdown()
  }

}
