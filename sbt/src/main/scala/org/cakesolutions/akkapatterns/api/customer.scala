package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.routing.Directives
import spray.httpx.marshalling.MetaMarshallers

/**
 * @author janmachacek
 */
class CustomerService(implicit val actorSystem: ActorSystem) extends Directives with MetaMarshallers with DefaultTimeout {
  def customerActor = actorSystem.actorFor("/user/application/customer")


  val route =
    path("customers" / JavaUUID) { id =>
      get {
        complete {
          "Just you wait"
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
