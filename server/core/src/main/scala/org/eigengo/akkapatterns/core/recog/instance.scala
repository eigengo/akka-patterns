package org.eigengo.akkapatterns.core.recog

import akka.actor.{Props, FSM, Actor, ActorRef}
import concurrent.duration.FiniteDuration
import org.eigengo.akkapatterns.domain._
import com.github.sstone.amqp.{RpcClient, ConnectionOwner}
import concurrent.Future
import spray.json.{JsonParser, JsonReader}
import com.github.sstone.amqp.RpcClient.{Response, Request}
import com.github.sstone.amqp.Amqp.{Delivery, Publish}
import util.{Failure, Success}
import java.util.UUID
import com.rabbitmq.client.AMQP

// -- Everything else here is private to this package; it is not to be messed with outside. --

/**
 * Transaction state hierarchy
 */
private[recog] sealed trait RecogSessionState
private[recog] case object Idle extends RecogSessionState
private[recog] case object WaitingForMoreImages extends RecogSessionState
private[recog] case object WaitingForImageResult extends RecogSessionState
private[recog] case object Aborted extends RecogSessionState
private[recog] case object Corrupted extends RecogSessionState
private[recog] case object Completed extends RecogSessionState

private[recog] sealed trait RecogSessionData
private[recog] case object InactiveSession extends RecogSessionData
private[recog] case class ActiveSession(configuration: SessionConfiguration,
                                        acceptedFeatures: List[Feature]) extends RecogSessionData {

  /**
   * Computes the feature that should appear in the next image
   * @return ``Some(Feature)`` or ``None`` if the session is not expecting any more images
   */
  def requiredFeature: Option[Feature] =
    if (acceptedFeatures.size < configuration.requiredFeatures.size)
      Some(configuration.requiredFeatures(acceptedFeatures.size))
    else
      None

  /**
   * Updates this session with the ``recogResult`` of image processing
   *
   * @param recogResult the processing result
   * @return updated session
   */
  def withResult(recogResult: RecogResult): ActiveSession = {
    requiredFeature match {
      case Some(feature) => if (recogResult.accepted) copy(acceptedFeatures = feature :: acceptedFeatures) else this
      case None          => this
    }
  }

  /**
   * Computes whether the session has completed
   * @return ``true`` if ther are no more required images
   */
  def completed: Boolean = configuration.requiredFeatures.size == acceptedFeatures.size
}

private[recog] case class SenderResult(sender: ActorRef, result: RecogResult)

private[recog] case object GetSession

private[recog] trait AmqpOperations extends DefaultTimeout {
  this: Actor =>

  protected def amqpAsk[A : JsonReader](amqp: ActorRef)
                                   (exchange: String, routingKey: String, payload: AmqpPayload, headers: Map[String, AnyRef] = Map()): Future[A] = {
    import collection.JavaConversions._
    val reader = implicitly[JsonReader[A]]

    val builder = new AMQP.BasicProperties.Builder
    builder.headers(headers)

    implicit val executionContext = context.dispatcher
    import akka.pattern.ask

    (amqp ? Request(Publish(exchange, routingKey, payload, Some(builder.build())) :: Nil)).map {
      case Response(Delivery(_, _, _, body)::_) =>
        val s = new String(body)
        reader.read(JsonParser(s))
      case x => sys.error("Bad match " + x)
    }
  }

}

/**
 * @author janmachacek
 */
class RecogSessionActor(connectionActor: ActorRef) extends Actor
  with FSM[RecogSessionState, RecogSessionData] with AmqpOperations with RecogFormats with ImageEncoding {

  private val StartTimeout = FiniteDuration(20, scala.concurrent.duration.SECONDS)
  private val StepTimeout = FiniteDuration(300, scala.concurrent.duration.SECONDS)

  val amqp = ConnectionOwner.createChildActor(connectionActor, Props(new RpcClient()))

  startWith(Idle, InactiveSession)

  when(Idle, StartTimeout) {
    case Event(configuration: SessionConfiguration, InactiveSession) =>
      goto(WaitingForMoreImages) using ActiveSession(configuration, Nil)
  }

  when(WaitingForImageResult, StepTimeout) {
    case Event(SenderResult(sender, r@RecogResult(true)), session: ActiveSession) =>
      val newSession = session.withResult(r)
      if (newSession.completed) {
        sender ! RecogSessionCompleted(UUID.randomUUID().toString)
        goto(Completed) using newSession
      } else {
        sender ! RecogSessionAccepted(r)
        goto(WaitingForMoreImages) using newSession
      }
    case Event(SenderResult(sender, r@RecogResult(false)), session: ActiveSession) =>
      sender ! RecogSessionRejected(r)
      goto(WaitingForMoreImages) using session
  }

  when(WaitingForMoreImages, StepTimeout) {
    case Event(x: Image, session: ActiveSession) =>
      val realSender = sender
      implicit val executionContext = context.dispatcher

      amqpAsk[RecogResult](amqp)("amq.direct", "recog.key", mkImagePayload(x)) onComplete {
        case Success(recogResult) => self ! SenderResult(realSender, recogResult)
        case Failure(_) => self ! SenderResult(realSender, RecogResult(false))
      }

      goto(WaitingForImageResult)
  }

  def unhandled: StateFunction = {
    case Event(GetSession, session: ActiveSession) =>
      sender ! session
      stay()
    case Event(StateTimeout, _) =>
      goto(Aborted)
  }

  whenUnhandled(unhandled)
  when(Completed, StepTimeout)(unhandled)
  when(Aborted)(unhandled)
  when(Corrupted)(unhandled)

  private def saveAnd(f: TransitionHandler): TransitionHandler = {
    case a -> b =>
      stateData match {
        case session: ActiveSession =>
          println("** Saving " + session)
          // Save to DB
        case _ =>
      }
      f(a, b)
  }

  onTransition {
    saveAnd {
      {
        case _ -> WaitingForMoreImages => // log
        case _ -> Completed => // send SMS
        case _ -> _ => // do nothing
      }
    }
  }

  initialize
}
