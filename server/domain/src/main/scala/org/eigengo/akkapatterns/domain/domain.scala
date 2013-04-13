package org.eigengo.akkapatterns

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
   *
   * NOTE: consider the alternative, using a `case class CustomerReference(id: UUID)`
   *       which is slightly more verbose but ensures type safety throughout the code.
   *       If your code has lots of UUIDs, you'll be *really* glad of type safe ids,
   *       trust me!
   */
  type CustomerReference = UUID

  /**
   * Type alias for user identity
   */
  type UserReference = UUID

}
