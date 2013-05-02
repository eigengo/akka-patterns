package org.eigengo.akkapatterns.core

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Await
import org.eigengo.akkapatterns.core.ApplicationActor.Start
import com.github.sstone.amqp.ConnectionOwner
import com.rabbitmq.client.ConnectionFactory
import org.eigengo.akkapatterns.core.recog.RecogCoordinatorActor
import org.eigengo.akkapatterns.core.authentication.LoginActor
import akka.routing.FromConfig

trait AmqpServerCore {
  def connectionActor: ActorRef
}

trait LocalAmqpServerCore extends AmqpServerCore {
  def actorSystem: ActorSystem

  // AMQP business
  lazy val connectionFactory = new ConnectionFactory()
  connectionFactory.setHost("localhost")
  connectionFactory.setVirtualHost("/")

  // create a "connection owner" actor, which will try and reconnect automatically if the connection is lost
  lazy val connectionActor = actorSystem.actorOf(Props(new ConnectionOwner(connectionFactory)))
}

trait ServerCore {
  this: AmqpServerCore =>
  def actorSystem: ActorSystem

  lazy val recogCoordinator = actorSystem.actorOf(Props(classOf[RecogCoordinatorActor], connectionActor))
  lazy val messageDelivery = actorSystem.actorOf(Props[MessageDeliveryActor].withDispatcher("low-priority-dispatcher"))
  lazy val userActor = actorSystem.actorOf(Props(classOf[UserActor], messageDelivery))
  lazy val loginActor = actorSystem.actorOf(Props(classOf[LoginActor], messageDelivery))
}
