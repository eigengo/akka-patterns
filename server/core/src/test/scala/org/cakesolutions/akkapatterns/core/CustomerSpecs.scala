package org.cakesolutions.akkapatterns.core

import org.cakesolutions.akkapatterns.{TestCustomerData, NoActorSpecs, CleanMongo, ActorSpecs}
import org.cakesolutions.akkapatterns.domain.{CustomerUserKind, UserDetailT, CustomerMongo}
import org.cakesolutions.akkapatterns.MongoCollectionFixture.Fix
import java.util.UUID


class CustomerSpecs extends NoActorSpecs with CleanMongo with CustomerMongo with TestCustomerData with Neo4JFixtures {

  neo4jFixtures

  val controller = new CustomerController

  "Customer actor" should {
    "be updateable" in new Fix("customers") {
        val jan = controller.get(TestCustomerJanId)
        val auth = UserDetailT(RootUser.id, CustomerUserKind(jan.id))

        controller.update(auth, jan.copy(lastName = "changed"))
        controller.get(TestCustomerJanId) !== jan
    }

    "not allow the wrong user to update" in new Fix("customers") {
      val jan = controller.get(TestCustomerJanId)
      val bad = UserDetailT(RootUser.id, CustomerUserKind(UUID.randomUUID()))

      controller.update(bad, jan.copy(lastName = "changed")) must throwA[IllegalArgumentException]
    }
  }
}
