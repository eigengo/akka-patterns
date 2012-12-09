package org.cakesolutions.akkapatterns

import java.util.UUID

package object domain {

  /**
   * The common base for all errors in our application
   */
  trait ApplicationFailure

  type Identity = UUID

  /**
   * Type alias for user identity
   */
  type UserReference = UUID

}
