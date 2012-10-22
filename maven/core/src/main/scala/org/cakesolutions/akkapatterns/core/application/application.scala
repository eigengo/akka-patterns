package org.cakesolutions.akkapatterns.core.application

import org.cakesolutions.akkapatterns.core.{Started, Stop, Start}
import akka.actor.{Props, Actor}
import org.cakesolutions.akkapatterns.domain.Configured
import com.mongodb.casbah.MongoDB

case class GetImplementation()
case class Implementation(title: String, version: String, build: String)

case class PoisonPill()

class ApplicationActor extends Actor {

  def receive = {
    case GetImplementation() =>
      val manifestStream = getClass.getResourceAsStream("/META-INF/MANIFEST.MF")
      val manifest = new java.util.jar.Manifest(manifestStream)
      val title = manifest.getMainAttributes.getValue("Implementation-Title")
      val version = manifest.getMainAttributes.getValue("Implementation-Version")
      val build = manifest.getMainAttributes.getValue("Implementation-Build")
      manifestStream.close()

      sender ! Implementation(title, version, build)

    case Start() =>
      context.actorOf(Props[CustomerActor], "customer")
      context.actorOf(Props[UserActor], "user")

      sender ! Started()

    /*
     * Stops this actor and all the child actors.
     */
    case Stop() =>
      context.children.foreach(context.stop _)

    case PoisonPill() =>
      sys.exit(-1)
  }

}

trait MongoCollections extends Configured {
  def customers = configured[MongoDB].apply("customers")
  def users = configured[MongoDB].apply("users")

}
