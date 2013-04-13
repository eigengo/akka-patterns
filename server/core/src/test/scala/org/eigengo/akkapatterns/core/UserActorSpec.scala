package org.eigengo.akkapatterns.core

import akka.testkit.TestActorRef
import org.eigengo.akkapatterns.{MongoCollectionFixture, TestMongo, ActorSpecs}
import org.eigengo.akkapatterns.domain.{SuperuserKind, User}
import org.eigengo.akkapatterns.MongoCollectionFixture.Fix

class UserActorSpec extends ActorSpecs with Neo4JFixtures {

  neo4jFixtures

  val actor = TestActorRef(new UserActor(testActor))

  "Basic user operations" should {
    "Find the root user" in {
      actor ! GetUserByUsername("root")
      expectMsgType[Option[User]] should beLike {
        case Some(user) if user.kind == SuperuserKind => ok
      }
    }
  }

}
