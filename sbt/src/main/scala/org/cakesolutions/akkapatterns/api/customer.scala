package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import cc.spray.Directives
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application._
import cc.spray.directives.JavaUUID
import akka.pattern.ask
import org.cakesolutions.akkapatterns.core.application.RegisterCustomer
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application.Get
import org.cakesolutions.akkapatterns.core.application.FindAll

/**
 * @author janmachacek
 */
class CustomerService(implicit val actorSystem: ActorSystem) extends Directives with Marshallers with Unmarshallers with DefaultTimeout with LiftJSON {
  def customerActor = actorSystem.actorFor("/user/application/customer")

  val route =
    path("customers" / JavaUUID) { id =>
      get {
        completeWith((customerActor ? Get(id)).mapTo[Option[Customer]])
      }
    } ~
    path("customers") {
      get {
        completeWith((customerActor ? FindAll()).mapTo[List[Customer]])
      } ~
      post {
        content(as[RegisterCustomer]) { rc =>
          completeWith((customerActor ? rc).mapTo[Either[NotRegisteredCustomer, RegisteredCustomer]])
        }
      }
    }

}
