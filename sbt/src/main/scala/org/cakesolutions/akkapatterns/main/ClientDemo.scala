package org.cakesolutions.akkapatterns.main

import akka.actor.{Props, ActorSystem}
import org.cakesolutions.akkapatterns.AmqpIO
import com.aphelia.amqp.{ConnectionOwner, RpcClient}
import com.aphelia.amqp.Amqp.{Publish, QueueParameters}
import java.io.ByteArrayOutputStream
import com.aphelia.amqp.RpcClient.{Response, Request}
import akka.pattern.ask
import akka.util.Timeout
import util.{Failure, Success}
import com.rabbitmq.client.ConnectionFactory

/**
 * @author janmachacek
 */
object ClientDemo {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(100000L)

  def main(args: Array[String]) {
    val actorSystem = ActorSystem("AkkaPatterns")

    // RabbitMQ connection factory
    val connectionFactory = new ConnectionFactory()
    connectionFactory.setHost("localhost")
    connectionFactory.setVirtualHost("/")

    // create a "connection owner" actor, which will try and reconnect automatically if the connection is lost
    val connection = actorSystem.actorOf(Props(new ConnectionOwner(connectionFactory)))

    // make a client actor
    val client = ConnectionOwner.createChildActor(connection, Props(new RpcClient()))

    Thread.sleep(1000)

    // publish a request
    val os = new ByteArrayOutputStream()
    // header
    os.write(0xca)
    os.write(0xac)
    os.write(0x00)
    os.write(0x10)

    // len 1
    os.write(0x00)
    os.write(0x00)
    os.write(0x00)
    os.write(0x00)

    // len 2
    os.write(0x00)
    os.write(0x00)
    os.write(0x00)
    os.write(0x00)

    val publish = Publish("amq.direct", "demo.key", os.toByteArray)
    client ? Request(publish :: Nil) onComplete {
      case Success(response: Response) => println(response)
      case x => println("Bantha poodoo!" + x)
    }
  }
}
