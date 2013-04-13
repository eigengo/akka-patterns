package org.eigengo.akkapatterns.domain

import com.typesafe.config.ConfigFactory
import com.mongodb.{ServerAddress, WriteConcern}
import scala.collection.JavaConversions._
import akka.contrib.jul.JavaLogging

object Settings extends JavaLogging {

  // https://groups.google.com/d/topic/scala-user/wzguzEJtLaI/discussion
  private val overrides = ConfigFactory.load("local")
  private val config = overrides.withFallback(ConfigFactory.load())

  private def unmerged(path: String) =
    if (overrides.hasPath(path)) overrides.getConfig(path)
    else config.getConfig(path)

  case class Cassandra(cluster: String, connections: Int, hosts: String)
  object Cassandra {
    def apply(base: String) = {
      val c = config.getConfig(base)
      val cluster = c.getString("cluster")
      val connections = c.getInt("connections")
      val hosts = unmerged(base + ".hosts").entrySet().map{e =>
        e.getKey.replaceAll("\"", "") + ":" + e.getValue.unwrapped()
      }.mkString(",")
      new Cassandra(cluster, connections, hosts)
    }
  }

  case class Mongo(name: String, connections: Int, hosts: List[ServerAddress], concern: WriteConcern)
  object Mongo {
    def apply(base: String) = {
      val c = config.getConfig(base)
      val name = c.getString("name")
      val connections = c.getInt("connections")
      val concern = WriteConcern.valueOf(c.getString("concern"))
      val hosts = unmerged(base + ".hosts").entrySet().map{e =>
        new ServerAddress(e.getKey.replaceAll("\"", ""), e.getValue.unwrapped().asInstanceOf[Integer])
      }.toList
      new Mongo(name, connections, hosts, concern)
    }
  }

  case class Db(cassandra: Cassandra, mongo: Mongo)
  case class Main(db: Db)

  val main = try Main(
    Db(
      Cassandra("main.db.cassandra"),
      Mongo("main.db.mongo")
    )
  ) catch {
    case t: Throwable => log.error(t, "Settings.main") ; throw t
  }

  val test = try Main(
    Db(
      Cassandra("test.db.cassandra"),
      Mongo("test.db.mongo")
    )
  ) catch {
    case t: Throwable => log.error(t, "Settings.test") ; throw t
  }
}
