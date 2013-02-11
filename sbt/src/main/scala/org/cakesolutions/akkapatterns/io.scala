package org.cakesolutions.akkapatterns

import akka.actor.{Props, Actor, ActorSystem}
import spray.io.IOExtension
import com.typesafe.config.ConfigFactory
import spray.client.HttpClient
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

  lazy val httpClient = actorSystem.actorOf(
    Props(new HttpClient(ConfigFactory.parseString("spray.can.client.ssl-encryption = on")))
  )

}

/**
 * Convenience ``HttpIO`` implementation that can be mixed in to actors.
 */
trait ActorHttpIO extends HttpIO {
  this: Actor =>

  final implicit def actorSystem = context.system
}

/**
 * Provides connection & access to the AMQP broker
 */
trait AmqpIO {
  implicit def actorSystem: ActorSystem

  // prepare the AMQP connection factory
  final lazy val connectionFactory = new ConnectionFactory(); connectionFactory.setHost("localhost")
  // connect to the AMQP exchange
  final lazy val amqpExchange = ExchangeParameters(name = "amq.direct", exchangeType = "", passive = true)

  // create a "connection owner" actor, which will try and reconnect automatically if the connection ins lost
  val connection = actorSystem.actorOf(Props(new ConnectionOwner(connectionFactory)))

}

/**
 * Convenience ``AmqpIO`` implementation that can be mixed in to actors.
 */
trait ActorAmqpIO extends AmqpIO {
  this: Actor =>
  final implicit def actorSystem = context.system

}