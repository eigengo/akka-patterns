package org.cakesolutions.akkapatterns.core.application

import akka.actor.Actor
import java.util.UUID
import org.cakesolutions.akkapatterns.domain.{User, Configured, Customer}
import com.mongodb.casbah.{MongoCollection, MongoDB}
import org.specs2.internal.scalaz.Identity
import org.cakesolutions.akkapatterns.domain

/**
 * Registers a customer and a user. After registering, we have a user account for the given customer.
 *
 * @param customer the customer
 * @param user the user
 */
case class RegisterCustomer(customer: Customer, user: User)

/**
 * Reply to successful customer registration
 * @param customer the newly registered customer
 * @param user the newly registered user
 */
case class RegisteredCustomer(customer: Customer, user: User)

/**
 * Reply to unsuccessful customer registration
 * @param code the error code for the failure reason
 */
case class NotRegisteredCustomer(code: String) extends Failure

/**
 * CRUD operations for the [[org.cakesolutions.akkapatterns.domain.Customer]]s
 */
trait CustomerOperations extends TypedCasbah with SearchExpressions {
  def customers: MongoCollection

  def getCustomer(id: domain.Identity) = customers.findOne(entityId(id)).map(mapper[Customer])

  def findAllCustomers() = customers.find().map(mapper[Customer]).toList

  def insertCustomer(customer: Customer) = {
    customers += serialize(customer)
    customer
  }

  def registerCustomer(customer: Customer)(ru: RegisteredUser): Either[Failure, RegisteredCustomer] = {
    customers += serialize(customer)
    Right(RegisteredCustomer(customer, ru.user))
  }

}

class CustomerActor extends Actor with Configured with CustomerOperations with UserOperations with MongoCollections {

  protected def receive = {
    case Get(id) =>
      sender ! getCustomer(id)

    case FindAll() =>
      sender ! findAllCustomers()

    case Insert(customer: Customer) =>
      sender ! insertCustomer(customer)

    case RegisterCustomer(customer, user) =>
      import scalaz._
      import Scalaz._

      sender ! (registerUser(user) >>= registerCustomer(customer))

  }
}
