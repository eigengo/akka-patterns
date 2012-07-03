package org.cakesolutions.akkapatterns.api

import cc.spray.test.SprayTest
import akka.util.Duration
import java.util.concurrent.TimeUnit
import akka.actor.ActorRef
import cc.spray.RequestContext
import cc.spray.http._

trait RootSprayTest extends SprayTest {
  protected def testRoot(request: HttpRequest, timeout: Duration = Duration(10000, TimeUnit.MILLISECONDS))
                        (root: ActorRef): ServiceResultWrapper = {
    val routeResult = new RouteResult
    root !
      RequestContext(
        request = request,
        responder = routeResult.requestResponder,
        unmatchedPath = request.path
      )

    // since the route might detach we block until the route actually completes or times out
    routeResult.awaitResult(timeout)
    new ServiceResultWrapper(routeResult, timeout)
  }

}
