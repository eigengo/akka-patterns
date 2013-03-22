package org.cakesolutions.akkapatterns.domain

import spray.json.DefaultJsonProtocol
import org.cakesolutions.scalad.mongo.sprayjson.{SprayMongoCollection, UuidMarshalling}
import com.mongodb.DB

case class Customer(id: CustomerReference,
                    firstName: String, lastName: String,
                    email: String, addresses: Seq[Address])

case class Address(line1: String, line2: String, line3: String)

trait CustomerFormats extends DefaultJsonProtocol with UuidMarshalling {

  implicit val AddressFormat = jsonFormat3(Address)
  implicit val CustomerFormat = jsonFormat5(Customer)

}

trait CustomerMongo extends CustomerFormats {
  this: Configured =>
  import org.cakesolutions.scalad.mongo.sprayjson._

  protected implicit val CustomerProvider = new SprayMongoCollection[Customer](configured[DB], "customers", "id":>1)
}