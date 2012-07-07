package org.cakesolutions.akkapatterns.core.application

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}

class NotificationActorSpec extends TestKit(ActorSystem()) with Specification with ImplicitSender {

  "x" in {
    val n = TestActorRef[NotificationActor]

    val person = "Jan Machacek"
    n ! Notify(person)

    // D_expectAndReply(GetAddress(person), List("A"))
    // D_expectMsgAllOf(Message("A", "Dummy body"))

    success
  }

}
