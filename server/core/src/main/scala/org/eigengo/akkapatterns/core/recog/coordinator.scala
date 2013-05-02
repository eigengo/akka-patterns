package org.eigengo.akkapatterns.core.recog

import akka.actor.{Kill, Props, ActorRef, Actor}
import org.eigengo.akkapatterns.domain._
import akka.util.Timeout
import java.util.UUID

/**
 * Submits the ``image`` to the session identified by ``recogSessionId``
 * @param recogSessionId the recognition session
 * @param image the image to be submitted
 */
case class ProcessImage(recogSessionId: RecogSessionId, image: Image)

/**
 * Begins the recognition session
 */
case object Begin

/**
 * Kills active session identified by id
 *
 * @param id the session identity
 */
case class KillActiveSession(id: RecogSessionId)

/**
 * Finds all active recognition sessions
 */
case object FindActiveSessions

/**
 * @author janmachacek
 */
class RecogCoordinatorActor(connectionActor: ActorRef) extends Actor {

  private def findInstanceActor(id: RecogSessionId): ActorRef = {
    context.actorFor(id.toString)
  }

  implicit val executionContext = context.dispatcher

  def receive = {
    case Begin =>
      val id = UUID.randomUUID()
      val instanceActor = context.actorOf(Props(classOf[RecogSessionActor], connectionActor), id.toString)
      instanceActor ! SessionConfiguration(FaceFeature :: Nil)
      sender ! id
    case ProcessImage(id, image) =>
      findInstanceActor(id).tell(image, sender)
    case KillActiveSession(id) =>
      findInstanceActor(id) ! Kill
    case FindActiveSessions =>
      sender ! context.children.map(c => UUID.fromString(c.path.name)).toList
  }
}
