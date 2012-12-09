package org.cakesolutions.akkapatterns.core

import akka.actor.{ActorRef, Actor}
import org.cakesolutions.akkapatterns.domain._
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
case class NotRegisteredCustomer(code: String) extends ApplicationFailure

/**
 * CRUD operations for the [[org.cakesolutions.akkapatterns.domain.Customer]]s
 */
trait CustomerOperations {
  // def customers: MongoCollection

  def getCustomer(id: domain.Identity): Option[Customer] = None

  def findAllCustomers(): List[Customer] = List()

  def insertCustomer(customer: Customer): Customer = {
    //customers += serialize(customer)
    customer
  }

  def registerCustomer(customer: Customer)(ru: RegisteredUser): Either[ApplicationFailure, RegisteredCustomer] = {
    //customers += serialize(customer)
    Right(RegisteredCustomer(customer, ru.user))
  }

}

/**
 * Performs the customer operations
 */
class CustomerActor(messageDelivery: ActorRef) extends Actor with CustomerOperations with UserOperations {

  def receive = {
    case _ => // TODO: complete me by moving me to Scalad
  }
}
