package org.cakesolutions.akkapatterns.domain

import spray.json._
import org.cakesolutions.akkapatterns.UuidFormats

/**
 * The user record, which stores the identtiy, the username and the password
 *
 * @author janmachacek
 */
case class User(id: UserReference, username: String, hashedPassword: String,
                 email: String, mobile: Option[String], firstName: String, lastName: String,
                 kind: UserKind) {

  def resetPassword(newPassword: String): User = this

  def checkPassword(password: String): Boolean = true

  def isTrustedDevice(signature: String): Boolean = false

}

/**
 * The various user kinds or roles we have; each role can have additional attributes we store about it
 */
sealed trait UserKind
case object SuperuserKind extends UserKind
case class CustomerUserKind(customerReference: CustomerReference) extends UserKind
case object GuestUserKind extends UserKind

/**
 * The user detail about an authenticated user. It contains the user ``id`` and the detailm which further refines
 * the kind of user we're dealing with.
 *
 * @param userReference the identity of the authenticated user
 * @param kind the user kind
 * @tparam A the type of the kind of user
 */
case class UserDetailT[A <: UserKind](userReference: UserReference, kind: A)

/**
 * Trait that contains the [[spray.json.JsonFormat]] instances for the user
 * management
 */
trait UserFormats extends DefaultJsonProtocol with UuidFormats {

  implicit object UserKindFormat extends JsonFormat[UserKind] {
    private val Superuser = JsString("superuser")
    private val Customer  = JsString("customer")
    private val Guest     = JsString("guest")
    private val CustomerUserKindFormat = jsonFormat1(CustomerUserKind)

    def write(obj: UserKind) = obj match {
      case SuperuserKind       => JsObject(("kind", Superuser))
      case GuestUserKind       => JsObject(("kind", Guest))
      case k: CustomerUserKind => JsObject(("kind", Customer), ("value", CustomerUserKindFormat.write(k)))
    }

    def read(json: JsValue) = json match {
      case JsObject(fields) =>
        fields.get("kind") match {
          case Some(Superuser) => SuperuserKind
          case Some(Guest)     => GuestUserKind
          case Some(Customer)  => CustomerUserKindFormat read fields("value")
          case _               => sys.error("bad kind")
        }
      case _                   => sys.error("bad json")
    }
  }

  implicit val UserFormat = jsonFormat8(User)

}