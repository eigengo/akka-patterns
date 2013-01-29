package org.cakesolutions.akkapatterns

import akka.actor.{Props, Actor, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import com.typesafe.config.ConfigFactory
import spray.client.HttpConduit
import com.aphelia.amqp.ConnectionOwner
import com.aphelia.amqp.Amqp.ExchangeParameters
import com.rabbitmq.client.ConnectionFactory

/**
 * Instantiates & provides access to Spray's ``IOBridge``.
 *
 * @author janmachacek
 */
trait HttpIO {
  implicit def actorSystem: ActorSystem
  
  lazy val ioBridge = IOExtension(actorSystem).ioBridge() // new IOBridge(actorSystem).start()

  private lazy val httpClient = actorSystem.actorOf(
    Props(new HttpClient(ioBridge, ConfigFactory.parseString("spray.can.client.ssl-encryption = on")))
  )

  def makeConduit(host: String) =
    actorSystem.actorOf(Props(new HttpConduit(httpClient, host, port = 443, sslEnabled = true)))

}

/**
 * Convenience ``HttpIO`` implementation that can be mixed in to actors.
 */
trait ActorHttpIO extends HttpIO {
  this: Actor =>

  final implicit def actorSystem = context.system
}

/*
 * Provides connection & access to the AMQP broker -- to be completed in the next release of akka-patterns
 */
trait AmqpIO {
  implicit def actorSystem: ActorSystem

  // prepare the AMQP connection factory
  final lazy val connectionFactory = new ConnectionFactory(); connectionFactory.setHost("localhost")

  // create a "connection owner" actor, which will try and reconnect automatically if the connection is lost
  val connection = actorSystem.actorOf(Props(new ConnectionOwner(connectionFactory)))

}

/*
 * Convenience ``AmqpIO`` implementation that can be mixed in to actors.
 */
trait ActorAmqpIO extends AmqpIO {
  this: Actor =>
  final implicit def actorSystem = context.system

}
