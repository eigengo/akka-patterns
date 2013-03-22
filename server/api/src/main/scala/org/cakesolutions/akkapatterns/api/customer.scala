package org.cakesolutions.akkapatterns.api

import spray.routing.HttpService
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.CustomerController
import akka.util.Timeout
import scala.concurrent.Future

trait CustomerService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  private val customerController = new CustomerController

  val customerRoute =
    path("customers" / JavaUUID) { id =>
      get {
        complete {
          // when using controllers, we have to explicitly create the Future here
          // it is not necessary to add the T information, but it helps with API documentation.
          Future[Customer] {
             customerController.get(id)
          }
        }
      } ~
      post {
        authenticate(validCustomer) { ud =>
          // if we authenticated only validUser or validSuperuser
          handleWith { customer: Customer =>
            Future[Customer] {
              customerController.update(ud, customer)
            }
          }
        }
      }
    }
}
