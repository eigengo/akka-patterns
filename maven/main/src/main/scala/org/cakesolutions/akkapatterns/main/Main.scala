package org.cakesolutions.akkapatterns.main

import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain.Configuration
import org.cakesolutions.akkapatterns.core.Core
import org.cakesolutions.akkapatterns.api.Api
import org.cakesolutions.akkapatterns.web.Web
import com.mongodb.casbah.MongoConnection

object Main extends App {
  // -javaagent:/Users/janmachacek/.m2/repository/org/springframework/spring-instrument/3.1.1.RELEASE/spring-instrument-3.1.1.RELEASE.jar -Xmx512m -XX:MaxPermSize=256m

  implicit val system = ActorSystem("AkkaPatterns")

  class Application(val actorSystem: ActorSystem) extends Core with Api with Web with Configuration {
    configure {
      val mongoDb = MongoConnection()("akka-patterns")

      mongoDb
    }
  }

  new Application(system)

  sys.addShutdownHook {
    system.shutdown()
  }

}
