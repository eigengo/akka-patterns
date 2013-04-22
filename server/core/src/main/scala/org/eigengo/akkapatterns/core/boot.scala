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
  val connectionFactory = new ConnectionFactory()
  connectionFactory.setHost("localhost")
  connectionFactory.setVirtualHost("/")

  // create a "connection owner" actor, which will try and reconnect automatically if the connection is lost
  val connectionActor = actorSystem.actorOf(Props(new ConnectionOwner(connectionFactory)))
}

trait ServerCore {
  this: AmqpServerCore =>
  def actorSystem: ActorSystem

  val recogCoordinator = actorSystem.actorOf(Props(new RecogCoordinatorActor(connectionActor)))
  val messageDelivery = actorSystem.actorOf(Props[MessageDeliveryActor].withDispatcher("low-priority-dispatcher"))
  val userActor = actorSystem.actorOf(Props(new UserActor(messageDelivery)))
  val loginActor = actorSystem.actorOf(Props(new LoginActor(messageDelivery)))
}
