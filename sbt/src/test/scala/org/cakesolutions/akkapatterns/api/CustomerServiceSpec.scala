package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain.{User, Customer}
import cc.spray.http.HttpMethods._
import org.cakesolutions.akkapatterns.core.application.{RegisteredCustomer, RegisterCustomer}
import java.util.UUID

/**
 * @author janmachacek
 */
class CustomerServiceSpec extends DefaultApiSpecification {
  implicit val service = rootService

  "Getting a known customer works" in {
    val customer = perform[Customer](GET, "/customers/00000000-0000-0000-0000-000000000000")

    customer must_== janMachacek
  }

  "Finding all customers works" in {
    val customers = perform[List[Customer]](GET, "/customers")

    customers must contain (janMachacek)
  }

  "Registering a customer" in {
    val rc = RegisterCustomer(
      joeBloggs,
      User(UUID.randomUUID(), "janm", "Like I'll tell you!"))
    val registered = perform[RegisterCustomer, RegisteredCustomer](POST, "/customers", rc)

    (registered.customer must_== joeBloggs) and
    (registered.user.username must_== "janm") and
    (registered.user.password must_!= "Like I'll tell you")
  }

}
