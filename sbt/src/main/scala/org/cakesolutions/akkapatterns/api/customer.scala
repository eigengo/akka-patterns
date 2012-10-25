package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import akka.pattern.ask
import spray.routing.Directives
import spray.httpx.marshalling.MetaMarshallers
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application._

/**
 * @author janmachacek
 */
class CustomerService(implicit val actorSystem: ActorSystem) extends Directives with Marshalling with MetaMarshallers with DefaultTimeout {
  def customerActor = actorSystem.actorFor("/user/application/customer")

  import scala.concurrent.ExecutionContext.Implicits.global

  val route =
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
}
