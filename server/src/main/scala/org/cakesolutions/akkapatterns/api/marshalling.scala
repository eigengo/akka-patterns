package org.cakesolutions.akkapatterns.api

import org.cakesolutions.akkapatterns.domain._
import org.cakesolutions.akkapatterns.core._
import spray.json.DefaultJsonProtocol
import org.cakesolutions.akkapatterns.UuidFormats
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.{CollectingMarshallingContext, MetaMarshallers, Marshaller}
import spray.http.{HttpEntity, StatusCode}

trait AllFormats extends UserFormats {

  implicit val NotRegisteredUserFormat = jsonFormat1(NotRegisteredUser)
  implicit val RegisteredUserFormat = jsonFormat1(RegisteredUser)

  implicit val AddressFormat = jsonFormat3(Address)
  implicit val CustomerFormat = jsonFormat5(Customer)
  implicit val RegisterCustomerFormat = jsonFormat2(RegisterCustomer)
  implicit val NotRegisteredCustomerFormat = jsonFormat1(NotRegisteredCustomer)
  implicit val RegisteredCustomerFormat = jsonFormat2(RegisteredCustomer)

}

trait Marshalling extends DefaultJsonProtocol with AllFormats with UuidFormats with SprayJsonSupport with MetaMarshallers {
  /**
   * Function that computers a HTTP status given value of type ``A``
   *
   * @tparam A the type a
   */
  type ErrorSelector[A] = A => StatusCode

  /**
   * Marshaller that uses some ``ErrorSelector`` for the value on the left to indicate that it is an error, even though
   * the error response should still be marshalled and returned to the caller.
   *
   * This is useful when you need to return validation or other processing errors, but need a bit more information than
   * just ``HTTP status 422`` (or, even worse simply ``400``).
   *
   * @param ma the marshaller for ``A`` (the error)
   * @param mb the marshaller for ``B`` (the success)
   * @param esa the error selector for ``A`` so that we know what HTTP status to return for the value on the left
   * @tparam A the type on the left
   * @tparam B the type on the right
   * @return the marshaller instance
   */
  implicit def errorSelectingEitherMarshaller[A, B](implicit ma: Marshaller[A], mb: Marshaller[B], esa: ErrorSelector[A]) =
    Marshaller[Either[A, B]] { (value, ctx) =>
      value match {
        case Left(a) =>
          val mc = new CollectingMarshallingContext()
          ma(a, mc)
          ctx.handleError(ErrorResponseException(esa(a), mc.entity))
        case Right(b) =>
          mb(b, ctx)
      }
    }

}

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends RuntimeException
