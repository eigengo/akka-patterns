package org.cakesolutions.akkapatterns.core.application

import java.util.UUID

/**
 * Base type for failures
 */
trait Failure {
  /**
   * The error code for the failure
   * @return the error code
   */
  def code: String
}

/**
 * Gets an entity identified by ``id``
 *
 * @param id the identity
 */
case class Get(id: UUID)

/**
 * Finds all entities
 */
case class FindAll()

/**
 * Inserts the given entity
 *
 * @param entity the entity to be inserted
 * @tparam A the type of A
 */
case class Insert[A](entity: A)

/**
 * Updates the given entity
 *
 * @param entity the entity to update
 * @tparam A the type of A
 */
case class Update[A](entity: A)