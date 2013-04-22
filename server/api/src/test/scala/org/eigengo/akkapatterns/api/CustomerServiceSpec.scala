package org.eigengo.akkapatterns.api

import spray.http.StatusCodes._
import org.eigengo.akkapatterns.TestCustomerData
import org.eigengo.akkapatterns.MongoCollectionFixture.Fix
import java.util.UUID


class CustomerServiceSpec extends ApiSpecs with CustomerService with TestCustomerData with Neo4JFixtures {

  implicit val route = handled(customerRoute)

  "/customers" should {
    "fail to GET an invalid customer" in {
      suppressNextException
      Get("/customers/invalid").failsWith(NotFound)
    }

    "GET a valid customer" in new Fix("customers") {
      val jan = customerController.get(TestCustomerJanId)
      Get(s"/customers/${jan.id}").returns(jan)
    }

    "require a valid token to POST" in new Fix("customers") {
      val jan = customerController.get(TestCustomerJanId)
      val update = jan.copy(lastName = "changed")
      suppressNextException
      Post(s"/customers/${jan.id}", update).failsWith(MethodNotAllowed)
    }

    "update with a valid POST" in new Fix("customers") {
      val jan = customerController.get(TestCustomerJanId)
      val update = jan.copy(lastName = "changed")

      {
        implicit val auth = Token(UUID.randomUUID()) // need correct token
        AuthPost(s"/customers/${jan.id}", update).returns(update)
      }

      Get(s"/customers/${jan.id}").returns(update)
    }.pendingUntilFixed("@janm399 needs to setup a test data fixture for tokens")

  }
}
