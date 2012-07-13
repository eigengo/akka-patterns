package org.cakesolutions.akkapatterns.core.application

import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import akka.actor.{Address, ActorSystem}
import org.specs2.mutable.Specification
import org.cakesolutions.akkapatterns.core.Core

/**
 * @author janmachacek
 */
class AddressBookActorSpec extends TestKit(ActorSystem()) with Specification with ImplicitSender with Core with SpecConfiguration {

  val actor = TestActorRef[AddressBookActor]

  "GetAddresses(p) replies with the person's addresses" in {
    actor ! GetAddresses("Jan")

    expectMsg(Address("Robert Robinson Avenue", "Oxford") ::
                   Address("Houldsworth Mill", "Reddish") ::
                   Nil)

    success
  }


  implicit def actorSystem = system
}
