package org.cakesolutions.akkapatterns.main

import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain.Configuration
import org.cakesolutions.akkapatterns.core.ServerCore
import org.cakesolutions.akkapatterns.web.Web
import org.cakesolutions.akkapatterns.api.Api
import akka.util.Timeout

object Main {

  def main(args: Array[String]) {
    implicit val system = ActorSystem("AkkaPatterns")

    class Application(val actorSystem: ActorSystem) extends ServerCore with Configuration with Web {
      implicit val timeout = Timeout(30000)
    }

    new Application(system)

    sys.addShutdownHook {
      system.shutdown()
    }
  }

}
