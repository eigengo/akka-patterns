package org.cakesolutions.akkapatterns.api

import akka.actor.ActorSystem
import cc.spray.Directives
import org.cakesolutions.akkapatterns.domain.Customer
import org.cakesolutions.akkapatterns.core.application.{Insert, FindAll, Get}
import cc.spray.directives.JavaUUID
import akka.pattern.ask

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
        content(as[Customer]) { customer =>
          completeWith((customerActor ? Insert(customer)).mapTo[Customer])
        }
      }
    }

}
