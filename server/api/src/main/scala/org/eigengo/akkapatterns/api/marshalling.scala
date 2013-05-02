package org.eigengo.akkapatterns.api

import org.eigengo.akkapatterns.domain._
import org.eigengo.akkapatterns.core._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import spray.httpx.marshalling.{CollectingMarshallingContext, MetaMarshallers, Marshaller}
import spray.http.{HttpEntity, StatusCode}
import org.eigengo.scalad.mongo.sprayjson.{DateMarshalling, UuidMarshalling}

// Pure boilerplate - please create a code generator (I'll be your *best* friend!)
trait ApiMarshalling extends DefaultJsonProtocol
  with UuidMarshalling with DateMarshalling {
  this: UserFormats =>

  implicit val NotRegisteredUserFormat = jsonFormat1(NotRegisteredUser)
  implicit val RegisteredUserFormat = jsonFormat1(RegisteredUser)

  implicit val ImplementationFormat = jsonFormat3(Implementation)
  implicit val SystemInfoFormat = jsonFormat2(SystemInfo)
}

case class ErrorResponseException(responseStatus: StatusCode, response: Option[HttpEntity]) extends Exception

trait EitherErrorMarshalling {

  /**
   * Marshaller that uses some ``ErrorSelector`` for the value on the left to indicate that it is an error, even though
   * the error response should still be marshalled and returned to the caller.
   *
   * This is useful when you need to return validation or other processing errors, but need a bit more information than
   * just ``HTTP status 422`` (or, even worse simply ``400``).
   *
   * Bring an implicit instance of this method into scope of your HttpServices to get the status code.
   *
   * @param status the status code to return for errors.
   * @param ma the marshaller for ``A`` (the error)
   * @param mb the marshaller for ``B`` (the success)
   * @tparam A the type on the left
   * @tparam B the type on the right
   * @return the marshaller instance
   */
  def errorSelectingEitherMarshaller[A, B](status: StatusCode)
                                          (implicit ma: Marshaller[A], mb: Marshaller[B]) =
    Marshaller[Either[A, B]] {
      (value, ctx) =>
        value match {
          case Left(a) =>
            val mc = new CollectingMarshallingContext()
            ma(a, mc)
            ctx.handleError(ErrorResponseException(status, mc.entity))
          case Right(b) =>
            mb(b, ctx)
        }
    }
}

trait EndpointMarshalling extends MetaMarshallers with SprayJsonSupport
  with ApiMarshalling
  with UserFormats with CustomerFormats
  with EitherErrorMarshalling