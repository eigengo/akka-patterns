package org.cakesolutions.akkapatterns

import java.util.UUID

package object domain {

  /**
   * The common base for all errors in our application
   */
  trait ApplicationFailure

  /**
   * Convenience type alias for any kind of user
   */
  type UserDetail = UserDetailT[UserKind]

  /**
   * Type alias for customer identity
   */
  type CustomerReference = UUID

  /**
   * Type alias for user identity
   */
  type UserReference = UUID

}
