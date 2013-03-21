package org.cakesolutions.akkapatterns

import domain._
import java.util.UUID

/**
 * The general idea is to have (verbose) mirroring of the data stored in the various database
 * "base" scripts, but in Scala land.
 *
 * It is also convenient to define additional data (somewhere else) and then persist it to the database,
 * but here we are essentially testing the mapping between the raw form and the Scala form.
 */
trait TestData {

  val UserGuest = User(
    UUID.fromString("994fc1f0-90a9-11e2-9e96-0800200c9a66"),
    "guest",
    "",
    "johndoe@example.com",
    None,
    "John",
    "Doe",
    GuestUserKind
  )

  val UserCustomer = User(
    UUID.fromString("7370f980-90aa-11e2-9e96-0800200c9a66"),
    "customer",
    "",
    "johndoe@example.com",
    Some("07777777777"),
    "John",
    "Doe",
    CustomerUserKind(UUID.fromString("82c6e890-90aa-11e2-9e96-0800200c9a66"))
  )

  val UserAdmin = User(
    UUID.fromString("c0a93190-90aa-11e2-9e96-0800200c9a66"),
    "root",
    "",
    "johndoe@example.com",
    None,
    "John",
    "Doe",
    UserAdmin
  )

}
