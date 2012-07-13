package org.cakesolutions.akkapatterns.core.application

import akka.actor.{Address, Actor}
import org.cakesolutions.akkapatterns.core.Configured
import javax.sql.DataSource

case class GetAddresses(person: String)

class AddressBookActor extends Actor with Configured {

  protected def receive = {
    case GetAddresses(person) =>
      val dataSource = configured[DataSource]
      println(dataSource.toString)

      sender ! Address("Robert Robinson Avenue", "Oxford") ::
               Address("Houldsworth Mill", "Reddish") ::
               Nil
  }
}
