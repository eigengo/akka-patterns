package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import java.util.UUID
import org.cakesolutions.akkapatterns.domain.{Configured, Customer}

case class Get(id: UUID)
case class FindAll()
case class Insert(customer: Customer)

/**
 * @author janmachacek
 */
class CustomerActor extends Actor with Configured {


  def receive = {
    case Get(id) =>
      sender ! None //customers.findOne(entityId(id)).map(mapper[Customer])

    case FindAll() =>
      sender ! List() //customers.find().map(mapper[Customer]).toList

    case Insert(customer) =>
      // customers += serialize(customer)
      sender ! customer
  }
}
