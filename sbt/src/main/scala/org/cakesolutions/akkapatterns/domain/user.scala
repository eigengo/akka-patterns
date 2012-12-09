package org.cakesolutions.akkapatterns.domain

import spray.json.DefaultJsonProtocol
import org.cakesolutions.akkapatterns.UuidFormats

/**
 * The user record, which stores the identtiy, the username and the password
 *
 * @author janmachacek
 */
case class User(id: UserReference, username: String, password: String)

/**
 * Trait that contains the [[spray.json.JsonFormat]] instances for the user
 * management
 */
trait UserFormats extends DefaultJsonProtocol with UuidFormats {
  implicit val UserFormat = jsonFormat3(User)

}