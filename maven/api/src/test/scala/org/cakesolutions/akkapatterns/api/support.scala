package org.cakesolutions.akkapatterns.api

import cc.spray.test.SprayTest
import java.util.concurrent.TimeUnit
import akka.actor.ActorRef
import cc.spray.RequestContext
import cc.spray.http._
import io.Source
import org.specs2.mutable.Specification
import org.cakesolutions.akkapatterns.test.{DefaultTestData, SpecConfiguration}
import org.cakesolutions.akkapatterns.core.Core
import concurrent.util.Duration

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

trait JsonSource {

  def jsonFor(location: String) = Source.fromInputStream(classOf[JsonSource].getResourceAsStream(location)).mkString

  def jsonContent(location: String) = Some(HttpContent(ContentType(MediaTypes.`application/json`), jsonFor(location)))

}

/**
 * Convenience trait for API tests
 */
trait ApiSpecification extends Specification with SpecConfiguration with RootSprayTest with Core with Api with Unmarshallers with Marshallers with LiftJSON {

  import cc.spray.typeconversion._

  protected def respond(method: HttpMethod, url: String, content: Option[HttpContent] = None)
                       (implicit root: ActorRef) = {
    val request = HttpRequest(method, url, content = content)
    testRoot(request)(root).response
  }

  protected def perform[A](method: HttpMethod, url: String, content: Option[HttpContent] = None)
                          (implicit root: ActorRef, unmarshaller: Unmarshaller[A]): A = {
    val request = HttpRequest(method, url, content = content)
    val response = testRoot(request)(root).response.content
    val obj = response.as[A] match {
      case Left(e) => throw new Exception(e.toString)
      case Right(r) => r
    }
    obj
  }

}

/**
 * Convenience trait for API tests; with default test data
 */
trait DefaultApiSpecification extends ApiSpecification with DefaultTestData with JsonSource