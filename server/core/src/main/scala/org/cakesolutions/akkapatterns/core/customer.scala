package org.cakesolutions.akkapatterns.core

import org.cakesolutions.akkapatterns.domain._
import org.cakesolutions.scalad.mongo.sprayjson._

/**
 * An alternative to using Actors (see UserActor) is to have traditional controllers.
 *
 * The advantage is clear: much simpler code that remains typesafe vs akka.ask.
 *
 * The disadvantage is that controllers cannot be distributed as easily as actors.
 * However, it is possible to refactor the internals of a controller to use an
 * Actor implementation if that is needed. And fire and forget tasks that are done as
 * part of a controller are most certainly prime candidates as Actor actions.
 */
class CustomerController extends CustomerMongo with Configured {

  val mongo = new SprayMongo

  /**
   * Registers a customer and a user. After registering, we have a user account for the given customer.
   *
   * @param customer the customer
   * @param user the user
   */
  def register(customer: Customer, user: User) {
    mongo.create(customer)
    ??? // still need to register the user in neo4j... I don't really get the whole user/customer distinction...
  }

  // it is better to unwrap the Option here, as Option[T] endpoints are awful ... it
  // is much better to catch NoElementExceptions in the FailureHandler and return an
  // appropriately formatted status response.
  def get(id: CustomerReference) = mongo.findOne[Customer]("id" :> id).get

  /**
   * @param userDetail the user making the call
   * @param customer the customer to be updated
   */
  def update(userDetail: UserDetailT[CustomerUserKind], customer: Customer) = userDetail.kind match {
    // this api design means that the admin user can't update customers... intentional?
    case CustomerUserKind(`customer`.id) =>
        mongo.findAndReplace("id" :> customer.id, customer)
        customer

    // this defensive coding may be unnecessary due to the way we are always called from the API
    // to avoid this form of work duplication, ensure that your codebase has a clear and well documented
    // policy for authentication and access checks. Indeed, the UserDetailT should not be passed around
    // if authentication checks have already been performed.
    case _ => throw new IllegalArgumentException(s"${userDetail.userReference} does not have access rights to any customers.")
  }

}
