package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain._
import org.cakesolutions.akkapatterns.core._

trait Marshalling extends UserFormats {

  implicit val NotRegisteredUserFormat = jsonFormat1(NotRegisteredUser)
  implicit val RegisteredUserFormat = jsonFormat1(RegisteredUser)

  implicit val AddressFormat = jsonFormat3(Address)
  implicit val CustomerFormat = jsonFormat5(Customer)
  implicit val RegisterCustomerFormat = jsonFormat2(RegisterCustomer)
  implicit val NotRegisteredCustomerFormat = jsonFormat1(NotRegisteredCustomer)
  implicit val RegisteredCustomerFormat = jsonFormat2(RegisteredCustomer)

}
