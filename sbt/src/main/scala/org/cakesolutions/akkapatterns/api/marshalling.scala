package org.cakesolutions.akkapatterns.api

import java.util.UUID
import spray.json._
import spray.httpx.marshalling._
import spray.httpx.SprayJsonSupport
import org.cakesolutions.akkapatterns.domain._
import org.cakesolutions.akkapatterns.core.application._

// might move upstream: https://github.com/spray/spray-json/issues/24
@deprecated("expecting to move upstream", "") trait UuidMarshalling {
  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}

trait Marshalling extends DefaultJsonProtocol with UuidMarshalling with SprayJsonSupport {

  implicit val UserFormat = jsonFormat3(User)
  implicit val NotRegisteredUserFormat = jsonFormat1(NotRegisteredUser)
  implicit val RegisteredUserFormat = jsonFormat1(RegisteredUser)

  implicit val AddressFormat = jsonFormat3(Address)
  implicit val CustomerFormat = jsonFormat5(Customer)
  implicit val RegisterCustomerFormat = jsonFormat2(RegisterCustomer)
  implicit val NotRegisteredCustomerFormat = jsonFormat1(NotRegisteredCustomer)
  implicit val RegisteredCustomerFormat = jsonFormat2(RegisteredCustomer)

  implicit val ImplementationFormat = jsonFormat3(Implementation)
  implicit val SystemInfoFormat = jsonFormat2(SystemInfo)
}
