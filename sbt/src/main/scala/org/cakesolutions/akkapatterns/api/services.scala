package org.cakesolutions.akkapatterns.api

import akka.actor.Actor
import spray.routing._

/**
 * @author janmachacek
 */
class RoutedHttpService(route: Route) extends Actor with HttpService {

  implicit def actorRefFactory = context

  def receive =
    runRoute(route)

}
