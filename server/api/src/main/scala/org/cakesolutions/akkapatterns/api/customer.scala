package org.cakesolutions.akkapatterns.api

import spray.routing.HttpService
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.UpdateCustomer
import akka.util.Timeout
import akka.actor.ActorRef

trait CustomerService extends HttpService {
  this: EndpointMarshalling with AuthenticationDirectives =>

  import akka.pattern.ask
  implicit val timeout: Timeout
  def customerActor: ActorRef

  val customerRoute =
    path("customers" / JavaUUID) { id =>
      get {
        complete {
          "Just you wait"
        }
      } ~
      post {
        authenticate(validCustomer) { ud =>
          // if we authenticated only validUser or validSuperuser
          handleWith { customer: Customer =>
            (customerActor ? UpdateCustomer(ud, customer)).mapTo[Customer]
            // then this call would not type-check!
          }
        }
      }
    }
}
