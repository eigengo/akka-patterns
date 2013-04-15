package org.eigengo.akkapatterns.api

import akka.actor.Actor
import spray._
import routing._
import org.eigengo.akkapatterns.core.{AmqpServerCore, LocalAmqpServerCore, ServerCore}
import akka.util.Timeout
import org.eigengo.akkapatterns.domain.Configured

trait Api extends Actor with HttpServiceActor
  with ServerCore
  with AmqpServerCore
  with FailureHandling
  with Tracking with Configured
  with EndpointMarshalling
  with DefaultAuthenticationDirectives
  with CustomerService
  with HomeService
  with UserService
  with RecogService {

  // used by the Akka ask pattern
  implicit val timeout = Timeout(10000)

  val routes =
      customerRoute ~
      homeRoute ~
      userRoute ~
      recogRoute

  def receive = runRoute (
    handleRejections(rejectionHandler)(
      handleExceptions(exceptionHandler)(
        trackRequestResponse(routes)
      )
    )
  )

}
