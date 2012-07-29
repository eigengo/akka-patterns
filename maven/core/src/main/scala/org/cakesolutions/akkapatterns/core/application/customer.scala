package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import java.util.UUID
import org.cakesolutions.akkapatterns.domain.{Configured, Customer}
import com.mongodb.casbah.MongoDB

case class Get(id: UUID)
case class FindAll()
case class Insert(customer: Customer)

/**
 * @author janmachacek
 */
class CustomerActor extends Actor with Configured with TypedCasbah with SearchExpressions {

  def customers = configured[MongoDB].apply("customers")

  protected def receive = {
    case Get(id) =>
      sender ! customers.findOne(entityId(id)).map(mapper[Customer])

    case FindAll() =>
      sender ! customers.find().map(mapper[Customer]).toList

    case Insert(customer) =>
      customers += serialize(customer)
      sender ! customer
  }
}
