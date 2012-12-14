package org.cakesolutions.akkapatterns.domain

import spray.json.DefaultJsonProtocol
import org.cakesolutions.akkapatterns.UuidFormats

case class Customer(id: CustomerReference,
                    firstName: String, lastName: String,
                    email: String, addresses: Seq[Address])

case class Address(line1: String, line2: String, line3: String)

trait CustomerFormats extends DefaultJsonProtocol with UuidFormats {

  implicit val AddressFormat = jsonFormat3(Address)
  implicit val CustomerFormat = jsonFormat5(Customer)

}