package org.cakesolutions.akkapatterns

import akka.actor.{Props, Actor, ActorSystem}
import spray.io.IOExtension
import com.typesafe.config.ConfigFactory
import com.github.sstone.amqp.ConnectionOwner
import com.github.sstone.amqp.Amqp.ExchangeParameters
import com.rabbitmq.client.ConnectionFactory
import spray.can.client.HttpClient
import akka.actor._
import spray.client.HttpConduit
import spray.can.client.DefaultHttpClient
import akka.spray.ExtensionActorRef

abstract class SendReceive(server: String, port: Int = 443, sslEnabled: Boolean = true) extends ExtensionId[ExtensionActorRef] {

  def createExtension(system: ExtendedActorSystem) = {
    val client = DefaultHttpClient(system)
    val conduitName = "http-conduit-" + port + "-" +
      (if (sslEnabled) "ssl" else "plain") +
      "-" + server
    val conduit = system.actorOf(
      props = Props(
        new HttpConduit(client, server, port = port, sslEnabled = sslEnabled)
      ),
      name = conduitName
    )
    new ExtensionActorRef(conduit)
  }

  def sendReceive(system: ActorSystem) = HttpConduit.sendReceive(get(system))

}

object Foursquare extends SendReceive("api.foursquare.com")
object ITunes extends SendReceive("buy.itunes.apple.com")
object ITunesSandbox extends SendReceive("sandbox.itunes.apple.com")
object Facebook extends SendReceive("graph.facebook.com")
object Twitter extends SendReceive("api.twitter.com")
object Nexmo extends SendReceive("rest.nexmo.com")



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