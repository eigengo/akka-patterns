package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import spray.httpx.SprayJsonSupport._
import spray.routing.Directives
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application._
import akka.pattern.ask
import org.cakesolutions.akkapatterns.core.application.RegisterCustomer
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application.Get
import org.cakesolutions.akkapatterns.core.application.FindAll
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.MetaMarshallers

/**
 * @author janmachacek
 */
class CustomerService(implicit val actorSystem: ActorSystem) extends Directives with Marshalling with MetaMarshallers with DefaultTimeout {
  def customerActor = actorSystem.actorFor("/user/application/customer")

  val route =
    path("customers" / JavaUUID) { id =>
      get {
        complete {
          import scala.concurrent.ExecutionContext.Implicits.global
          (customerActor ? Get(id)).mapTo[Option[Customer]]
        }
      }
    } ~
      path("customers") {
        get {
          complete{
        	 val bob = (customerActor ? FindAll()).mapTo[List[Customer]]
        	 bob
            }
        } ~
          post {
            content(as[RegisterCustomer]) { rc =>
              complete((customerActor ? rc).mapTo[Either[NotRegisteredCustomer, RegisteredCustomer]])
            }
          }
      }
}
