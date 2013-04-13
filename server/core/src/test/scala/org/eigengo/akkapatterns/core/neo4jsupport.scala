package org.eigengo.akkapatterns.core

import org.eigengo.akkapatterns.domain.{SuperuserKind, User, UserFormats}
import java.util.UUID

// TODO https://github.com/janm399/akka-patterns/issues/35
trait Neo4JFixtures extends TypedGraphDatabase with UserFormats with SprayJsonNodeMarshalling with UserGraphDatabaseIndexes {

  val RootUserPassword = "*******"
  val RootUser = User(UUID.fromString("a3372060-2b3b-11e2-81c1-0800200c9a66"), "root", "", "janm@eigengo.net", None, "Jan", "Machacek", SuperuserKind).resetPassword(RootUserPassword)

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

  def neo4jFixtures: Boolean = synchronized {
    ensureUserSanity
  }

}
