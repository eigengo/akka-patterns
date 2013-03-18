package org.cakesolutions.akkapatterns

import domain._
import java.util.UUID

/**
 * @author janmachacek
 */
trait TestData {

  object Users {

    def newUser(username: String): User = User(UUID.randomUUID(), username, "", "janm@cakesolutions.net", None, "F" + username, "L" + username, SuperuserKind).resetPassword("password")
  }

}
