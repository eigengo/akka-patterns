package org.cakesolutions.akkapatterns.core

import org.cakesolutions.akkapatterns.domain.{SuperuserKind, User, UserFormats}
import java.util.UUID

/**
 * Initial system sanity checks
 */
trait SanityChecks extends TypedGraphDatabase with UserFormats with SprayJsonNodeMarshalling
  with UserGraphDatabaseIndexes {

  val RootUserPassword = "*******"
  val RootUser = User(UUID.fromString("a3372060-2b3b-11e2-81c1-0800200c9a66"), "root", "", "janm@cakesolutions.net", None, "Jan", "Machacek", SuperuserKind).resetPassword(RootUserPassword)

  private def ensureUserSanity: Boolean = {
    findOneEntityWithIndex[User] { _.get("username", RootUser.username) } match {
      case None =>
        withTransaction {
          addOneWithIndex(RootUser) { (node, index) =>
            index.putIfAbsent(node, "username", RootUser.username)
            index.putIfAbsent(node, "id", RootUser.id.toString)
          }
        }
        true
      case Some(rootUser) =>
        rootUser.checkPassword(RootUserPassword)
    }
  }

  def ensureSanity: Boolean = synchronized {
    ensureUserSanity
  }

}
