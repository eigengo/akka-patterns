package org.eigengo.akkapatterns.api

import akka.util.Timeout
import spray.routing.{RequestContext, HttpService}
import org.eigengo.akkapatterns.domain.RecogSessionId
import scala.reflect.ClassTag
import spray.json.RootJsonFormat
import akka.actor.ActorRef

trait RecogService extends HttpService {
  this: EndpointMarshalling =>

  implicit val timeout: Timeout

  def recogCoordinator: ActorRef

  def image[A : ClassTag : RootJsonFormat](sessionId: RecogSessionId)(ctx: RequestContext) {
    // coordinatorActor
    /*
    (coordinator ? ProcessImage(transactionId, Base64.decodeBase64(ctx.request.entity.buffer))).mapTo[A] onSuccess {
      case r: A => ctx.complete[A](StatusCodes.OK, RawHeader("Access-Control-Allow-Origin", origin) :: Nil, r)
      case x    => ctx.complete(StatusCodes.InternalServerError)
    }
    */
  }

  val recogRoute =
    path("recog") {
      post {
        ctx: RequestContext => {
          ctx.complete("{}")
        }
      }
    }

}
