package org.cakesolutions.akkapatterns.core.application

import org.cakesolutions.akkapatterns.core.{Started, Stop, Start}
import akka.actor.{Props, ActorRef, Actor}
import akka.routing.RoundRobinRouter

case class Load(path: String)
case class Unload(path: String)

case class Loaded()
case class Unloaded()

case class Scan()
case class Scanned(actorsFound: Int)

case class GetImplementation()
case class Implementation(title: String, version: String, build: String)

case class PoisonPill()

class ApplicationActor extends Actor {

  private def isDeadLetters(actorRef: ActorRef) = actorRef == context.system.deadLetters

  protected def receive = {
    case GetImplementation() =>
      val manifestStream = getClass.getResourceAsStream("/META-INF/MANIFEST.MF")
      val manifest = new java.util.jar.Manifest(manifestStream)
      val title = manifest.getMainAttributes.getValue("Implementation-Title")
      val version = manifest.getMainAttributes.getValue("Implementation-Version")
      val build = manifest.getMainAttributes.getValue("Implementation-Build")
      manifestStream.close()

      sender ! Implementation(title, version, build)

    case Start() =>
      self ! Scan()

      sender ! Started()

    /*
     * Scans the application for the actors
     */
    case Scan() =>
      context.actorOf(
        props = Props[BombActor].withRouter(RoundRobinRouter(nrOfInstances = 10)),
        name = "bomb")

      sender ! Scanned(0)

    /*
     * Stops this actor and all the child actors.
     */
    case Stop() =>
      context.children.foreach(context.stop _)

    /*
     * Loads a known & configured user actor identified by path. Replies with
     * Either[CannotLoadException, Loaded]
     */
    case Load(path) =>
      sender ! Left(new CannotLoadException(path))

    /*
     * Stops and unloads the user actor identified by path. Replies with
     * Either[CannotUnloadException, Unloaded]
     */
    case Unload(path) =>
      val actorRef = context.actorFor(path)
      if (!isDeadLetters(actorRef)) {
        context.stop(actorRef)
        sender ! Right(Unloaded())
      } else {
        sender ! Left(new CannotUnloadException(path))
      }

    case PoisonPill() =>
      sys.exit(-1)
  }

}
