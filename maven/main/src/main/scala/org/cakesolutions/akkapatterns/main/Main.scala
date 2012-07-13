package org.cakesolutions.akkapatterns.main

import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.core.{Configuration, Core}
import org.cakesolutions.akkapatterns.api.Api
import org.cakesolutions.akkapatterns.web.Web
import javax.sql.ConnectionPoolDataSource
import org.apache.commons.dbcp.BasicDataSource
import org.hsqldb.jdbc.JDBCDriver

object Main extends App {
  // -javaagent:/Users/janmachacek/.m2/repository/org/springframework/spring-instrument/3.1.1.RELEASE/spring-instrument-3.1.1.RELEASE.jar -Xmx512m -XX:MaxPermSize=256m

  implicit val system = ActorSystem("AkkaPatterns")

  class Application(val actorSystem: ActorSystem) extends Core with Api with Web with Configuration {
    configure {
      val ds = new BasicDataSource()
      ds.setDriverClassName(classOf[JDBCDriver].getCanonicalName)
      ds.setUsername("sa")
      ds.setUrl("jdbc:hsqldb:mem:test")

      ds
    }
  }

  new Application(system)

  sys.addShutdownHook {
    system.shutdown()
  }

}
