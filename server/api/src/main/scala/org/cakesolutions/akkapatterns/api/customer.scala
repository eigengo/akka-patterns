package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.routing.Directives
import spray.httpx.marshalling.MetaMarshallers
import org.cakesolutions.akkapatterns.domain.{CustomerFormats, Customer}
import org.cakesolutions.akkapatterns.core.UpdateCustomer
import spray.httpx.SprayJsonSupport

/**
 * @author janmachacek
 */
class CustomerService(implicit val actorSystem: ActorSystem) extends Directives with MetaMarshallers with DefaultTimeout
  with DefaultAuthenticationDirectives with CustomerFormats with SprayJsonSupport {
  import akka.pattern.ask
  import concurrent.ExecutionContext.Implicits.global

  def customerActor = actorSystem.actorFor("/user/application/customer")

  val route =
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
