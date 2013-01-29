package org.cakesolutions.akkapatterns.api

import akka.actor.Actor
import spray.routing._
import util.control.NonFatal
import spray.http.StatusCodes._
import spray.util.LoggingContext

/**
 * @author janmachacek
 */
class RoutedHttpService(route: Route) extends Actor with HttpService {

  implicit def actorRefFactory = context

  implicit val handler = ExceptionHandler.fromPF {
    case NonFatal(ErrorResponseException(statusCode, entity)) => ctx =>
      ctx.complete(statusCode, entity)

    case NonFatal(e) => ctx =>
      ctx.complete(InternalServerError)
    }


  def receive = {
    runRoute(route)(handler, RejectionHandler.Default, context, RoutingSettings.Default, LoggingContext.fromActorRefFactory)
  }

}
