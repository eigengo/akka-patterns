package org.eigengo.akkapatterns.core.authentication

import akka.actor.Actor
import org.eigengo.akkapatterns.domain._


/**
 * Activates the user by verifying that the ``activationCode`` matches the one in the ``userToActivate``
 *
 * @param userToActivate the user to be activated
 * @param activationCode the activation code; the code must match the one in the current user record
 * @param newPassword the new password the user selected
 */
case class ActivateUser(userToActivate: UserReference, activationCode: String, newPassword: String)

/**
 * Acknowledges activation of user ``userReference``
 */
case class ActivatedUser(userReference: UserReference)

/**
 * Acknowledges update of user ``userReference``
 */
case class UpdatedUser(userReference: UserReference)


/**
 * Manages the user accounts: updates,  activates, deactivates; changes and resets passwords.
 */
class AccountActor extends Actor {

  def receive = {
    case _ =>
  }

}
