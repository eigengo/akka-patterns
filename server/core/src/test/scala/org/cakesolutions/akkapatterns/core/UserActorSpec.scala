package org.cakesolutions.akkapatterns.core

import akka.testkit.TestActorRef
import org.cakesolutions.akkapatterns.ActorSpecs

class UserActorSpec extends ActorSpecs with SanityChecks {

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
