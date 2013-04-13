package org.eigengo.akkapatterns.api

import java.util.UUID
import spray.testkit.Specs2RouteTest
import spray.http.{StatusCode, StatusCodes, HttpRequest}
import spray.http.HttpHeaders.RawHeader
import spray.httpx.RequestBuilding
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing._
import org.specs2.mutable.Specification
import akka.contrib.jul.JavaLogging
import spray.routing.{HttpService, Rejection}
import scala.reflect.ClassTag
import org.eigengo.akkapatterns.{NoActorSpecs, CleanMongo, ActorSpecs}
import org.eigengo.akkapatterns.core.{ServerCore, CoreActorRefs}
import akka.actor.ActorSystem
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import akka.util.Timeout
import akka.testkit.TestKit
import org.specs2.specification.{Step, Fragments}
import spray.util.LoggingContext
import spray.http.StatusCodes._
import org.eigengo.akkapatterns.domain.Configured

case class Token(token: UUID)

/** Provides default and easy authentication in testkit specs
  * on endpoints that use [[org.eigengo.akkapatterns.api.AuthenticationDirectives]]
  * for authentication.
  *
  * implicit AuthenticationToken doesn't work because of clashes with Spray :-(
  */
trait AuthenticatedTestkit {
  this: Specs2RouteTest =>

  private def addAuth(request: HttpRequest)(implicit token: Token) = {
    // AuthenticationDirectives expects x-token in the header, even for GET / DELETE
    request.copy(headers = RawHeader("x-token", token.token.toString) :: request.headers)
  }

  def AuthGet(uri: String)(implicit token: Token) = {
    addAuth(RequestBuilding.Get(uri))
  }

  def AuthPost[T](uri: String, content: T)
                 (implicit token: Token,
                  marshaller: Marshaller[T]) = {
    addAuth(RequestBuilding.Post(uri, content))
  }

  def AuthPut[T](uri: String, content: T)
                (implicit token: Token,
                 marshaller: Marshaller[T]) = {
    addAuth(RequestBuilding.Put(uri, content))
  }

  def AuthDelete(uri: String)(implicit token: Token) = {
    // AuthenticationDirectives expects x-token in the header, not the query
    addAuth(RequestBuilding.Delete(uri))
  }

}

// e.g.
//   Get("/").succeeds()
//   Get("/").returns(myExample)
//   val get = Get("/").receive[MyObject]
trait RouteTestNiceness {
  this: Specs2RouteTest with Specification with JavaLogging =>

  implicit val routeTestTimeout = RouteTestTimeout(Duration(5, TimeUnit.SECONDS))

  implicit class ImplicitRouteCheck(request: HttpRequest)
                                   (implicit route: Route, timeout: RouteTestTimeout) {

    // `specs` works, but sometimes IntelliJ thinks there are problems
    def specs[T](f: => T) {request ~> route ~> check(f)}

    def receive[T](implicit um: Unmarshaller[T]): T = request ~> route ~> check {
      try entityAs[T]
      catch {
        case up: Exception =>
          if (up.getMessage != null && up.getMessage.startsWith("MalformedContent"))
            failure(s"didn't get the response type we expected: ${response.entity}")
          else throw up
      }
    }

    // failures here might say application/json was unexpected. That probably means the
    // exception handler kicked in and gave us a failure message instead of a T.
    def returns[T](expected: T)(implicit um: Unmarshaller[T]) {
      receive[T] === expected
    }

    def returnsA[T](implicit um: Unmarshaller[T]) = {
      receive[T]
      success
    }

    def succeeds() {specs(status === StatusCodes.OK)}

    def succeedsWith(code: StatusCode) = specs {status === code}

    // note, the failure type may be different in production:
    // we use a different exception handler in testing.
    def failsWith(code: StatusCode) = succeedsWith(code)

    def rejectedAs[T <: Rejection: ClassTag] = specs {rejection must beAnInstanceOf[T]}
  }
}


trait TestFailureHandling {
  this: HttpService with Tracking =>

  def handled(route: Route) = handleRejections(testRejectionHandler)(handleExceptions(testExceptionHandler)(trackRequestResponse(route)))

  @volatile private var suppressed = false

  def suppressNextException() {
    suppressed = true
  }

  def testRejectionHandler: RejectionHandler = RejectionHandler.Default

  def testExceptionHandler(implicit log: LoggingContext) = ExceptionHandler.fromPF {
    case t: Throwable => ctx =>
      // this ensures we see any logs that would otherwise be swallowed
      // AND allows us to suppress expected exceptions with the "suppressException"
      // mutable operation
      if (!suppressed)
        log.error(t, ctx.request.toString())
      else
        suppressed = false
      ctx.complete(InternalServerError, t.toString)
  }
}


/** Common imports for our Specs2RouteTest and fires up our Core
  * Actor system. This way we can unit test the routes, but integrate
  * test with Core.
  */
trait ApiSpecs extends NoActorSpecs
with Specs2RouteTest
with RouteTestNiceness
with EndpointMarshalling
with JavaLogging
with DefaultAuthenticationDirectives
with AuthenticatedTestkit
with TestFailureHandling
with ServerCore with CoreActorRefs with CleanMongo with Tracking {
  this: HttpService =>

  def actorSystem = system // for ServerCore
  def actorRefFactory = system // for Specs2RouteTest


  //implicit val auth = Token(... some test data here ...)

  // we have to duplicate code here from ActorSpecs because Specs2RouteTest is INCOMPATIBLE with Testkit (go figure)
  sequential
  implicit val timeout = Timeout(10000)
  override def map(fs: => Fragments) = super.map(fs) ^ Step(system.shutdown())
}
