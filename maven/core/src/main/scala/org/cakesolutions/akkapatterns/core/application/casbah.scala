package org.cakesolutions.akkapatterns.core.application

import com.mongodb.casbah.Imports._
import java.util.UUID
import org.cakesolutions.akkapatterns.domain.{User, Address, Customer}

/**
 * Contains type classes that deserialize records from Casbah into "our" types.
 */
trait CasbahDeserializers {
  type CasbahDeserializer[A] = DBObject => A

  /**
   * Convenience method that picks the ``CasbahDeserializer`` for the type ``A``
   * @param deserializer implicitly given deserializer
   * @tparam A the type A
   * @return the deserializer for ``A``
   */
  def casbahDeserializer[A](implicit deserializer: CasbahDeserializer[A]) = deserializer

  private def inner[A: CasbahDeserializer](o: DBObject, field: String): A = casbahDeserializer[A].apply(o.as[DBObject](field))

  private def innerList[A: CasbahDeserializer](o: DBObject, field: String): Seq[A] = {
    val deserializer = casbahDeserializer[A]
    o.as[MongoDBList](field).map {
      inner => deserializer(inner.asInstanceOf[DBObject])
    }
  }

  implicit object AddressDeserializer extends CasbahDeserializer[Address] {
    def apply(o: DBObject) =
      Address(o.as[String]("line1"), o.as[String]("line2"), o.as[String]("line3"))
  }

  implicit object CustomerDeserializer extends CasbahDeserializer[Customer] {
    def apply(o: DBObject) =
      Customer(o.as[String]("firstName"), o.as[String]("lastName"), o.as[String]("email"),
        innerList[Address](o, "addresses"), o.as[UUID]("id"))
  }

  implicit object UserDeserializer extends CasbahDeserializer[User] {
    def apply(o: DBObject) = User(o.as[UUID]("id"), o.as[String]("username"), o.as[String]("password"))
  }

}

/**
 * Contains type classes that serialize "our" types into Casbah records.
 */
trait CasbahSerializers {
  type CasbahSerializer[A] = A => DBObject

  /**
   * Convenience method that picks the ``CasbahSerializer`` for the type ``A``
   * @param serializer implicitly given serializer
   * @tparam A the type A
   * @return the serializer for ``A``
   */
  def casbahSerializer[A](implicit serializer: CasbahSerializer[A]) = serializer

  implicit object AddressSerializer extends CasbahSerializer[Address] {
    def apply(address: Address) = {
      val builder = MongoDBObject.newBuilder

      builder += "line1" -> address.line1
      builder += "line2" -> address.line2
      builder += "line3" -> address.line2

      builder.result()
    }
  }

  implicit object UserSerializer extends CasbahSerializer[User] {
    def apply(user: User) = {
      val builder = MongoDBObject.newBuilder

      builder += "username" -> user.username
      builder += "password" -> user.password
      builder += "id" -> user.id

      builder.result()
    }
  }

  implicit object CustomerSerializer extends CasbahSerializer[Customer] {
    def apply(customer: Customer) = {
      val builder = MongoDBObject.newBuilder

      builder += "firstName" -> customer.firstName
      builder += "lastName" -> customer.lastName
      builder += "email" -> customer.email
      builder += "addresses" -> customer.addresses.map(AddressSerializer(_))
      builder += "id" -> customer.id

      builder.result()
    }
  }

}

/**
 * Contains convenience functions that can be used to find "entities-by-id"
 */
trait SearchExpressions {

  def entityId(id: UUID) = MongoDBObject("id" -> id)

//  def entityId(id: UUID) = MongoDBObject("id" -> id, "active" -> true)

}

/**
 * Mix this trait into your classes to gain the functionality of the serializers, deserializers and mappers.
 */
trait TypedCasbah extends CasbahDeserializers with CasbahSerializers {

  final def serialize[A: CasbahSerializer](a: A) = casbahSerializer[A].apply(a)

  final def deserialize[A: CasbahDeserializer](o: DBObject) = casbahDeserializer[A].apply(o)

  final def mapper[A: CasbahDeserializer] = {
    (o: DBObject) => deserialize[A](o)
  }

}