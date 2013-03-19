package org.cakesolutions.akkapatterns.core

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}

/**
 * @author janmachacek
 */
class UserActorSpec extends TestKit(ActorSystem()) with Specification with SanityChecks with ImplicitSender {
  sequential
  ensureSanity

  val actor = TestActorRef(new UserActor(testActor))

  "Basic user operations" should {

    "Find the root user" in {
      actor ! GetUserByUsername("root")
      expectMsg(Some(RootUser))
      success
    }
  }

}
