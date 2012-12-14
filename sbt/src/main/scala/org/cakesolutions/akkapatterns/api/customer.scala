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
          handleWith { customer: Customer =>
            (customerActor ? UpdateCustomer(ud, customer)).mapTo[Customer]
          }
        }
      }
    }
  /*
    path("customers" / JavaUUID) { id =>
      get {
        complete {
          (customerActor ? Get(id)).mapTo[Option[Customer]]
        }
      }
    } ~
    path("customers") {
      get {
        complete {
          (customerActor ? FindAll()).mapTo[List[Customer]]
        }
      } ~
        post {
          entity(as[RegisterCustomer]) { rc =>
            complete((customerActor ? rc).mapTo[Either[NotRegisteredCustomer, RegisteredCustomer]])
          }
        }
    }
    */
}
