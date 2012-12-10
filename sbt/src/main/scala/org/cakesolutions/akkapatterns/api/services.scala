package org.cakesolutions.akkapatterns.api

import akka.actor.Actor
import spray.routing._
import util.control.NonFatal
import spray.http.StatusCodes._

/**
 * @author janmachacek
 */
class RoutedHttpService(route: Route) extends Actor with HttpService {

  implicit def actorRefFactory = context

  implicit val handler = ExceptionHandler.fromPF {
    case NonFatal(ErrorResponseException(statusCode, entity)) => log => ctx =>
      ctx.complete(statusCode, entity)

    case NonFatal(e) => log => ctx =>
      log.error(e, "Error during processing of request {}", ctx.request)
      ctx.complete(InternalServerError)
    }


  def receive = {
    runRoute(route)(handler, RejectionHandler.Default, context, RoutingSettings.Default)
  }

}
