package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import cc.spray.Directives
import org.cakesolutions.akkapatterns.core.application.GetAddresses
import akka.pattern.ask
import org.cakesolutions.akkapatterns.domain.Address

/**
 * @author janmachacek
 */
class AddressBookService(implicit val actorSystem: ActorSystem) extends Directives with Marshallers with DefaultTimeout {
  def addressBook = actorSystem.actorFor("/user/application/addressBook")

  val route =
    path("addressbook") {
      get {
        completeWith(
          (addressBook ? GetAddresses("Jan")).mapTo[List[Address]]
        )
      }
    }

}
