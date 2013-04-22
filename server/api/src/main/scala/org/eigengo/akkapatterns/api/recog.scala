package org.eigengo.akkapatterns.api

import spray.routing.{Directives, RequestContext}
import org.eigengo.akkapatterns.domain.{DefaultTimeout, UuidFormats, RecogSessionId}
import scala.reflect.ClassTag
import spray.json.RootJsonFormat
import akka.actor.ActorRef
import org.eigengo.akkapatterns.core.recog._
import org.apache.commons.codec.binary.Base64
import spray.http.StatusCodes
import spray.http.HttpHeaders.RawHeader
import concurrent.ExecutionContext
import org.eigengo.akkapatterns.core.recog.ProcessImage
import org.eigengo.akkapatterns.core.recog.RecogSessionRejected
import org.eigengo.akkapatterns.core.recog.RecogSessionAccepted
import spray.routing.RequestContext
import spray.http.HttpHeaders.RawHeader

class RecogService(coordinator: ActorRef, origin: String)(implicit executionContext: ExecutionContext) extends Directives with CrossLocationRouteDirectives with EndpointMarshalling
  with DefaultTimeout with RecogFormats {
  val headers = RawHeader("Access-Control-Allow-Origin", origin) :: Nil

  import akka.pattern.ask

  def image(sessionId: RecogSessionId)(ctx: RequestContext) {
    (coordinator ? ProcessImage(sessionId, Base64.decodeBase64(ctx.request.entity.buffer))) onSuccess {
      case x: RecogSessionAccepted  => ctx.complete(StatusCodes.Accepted,            headers, x)
      case x: RecogSessionRejected  => ctx.complete(StatusCodes.BadRequest,          headers, x)
      case x: RecogSessionCompleted => ctx.complete(StatusCodes.OK,                  headers, x)
      case x                        => ctx.complete(StatusCodes.InternalServerError, headers, x.toString)
    }
  }

  val route =
    path("recog") {
      post {
        complete {
          (coordinator ? Begin).map(_.toString)
        }
      }
    } ~
    path("recog" / JavaUUID) { sessionId =>
      post {
        image(sessionId)
      }
    }

}
