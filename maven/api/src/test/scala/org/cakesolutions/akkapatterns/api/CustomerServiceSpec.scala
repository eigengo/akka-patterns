package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain.Customer
import cc.spray.http.HttpMethods._

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

  "Saving a customer gets the saved one" in {
    val customer = perform[Customer](POST, "/customers", jsonContent("/org/cakesolutions/akkapatterns/api/customers-post.json"))

    customer must_== joeBloggs
  }

}
