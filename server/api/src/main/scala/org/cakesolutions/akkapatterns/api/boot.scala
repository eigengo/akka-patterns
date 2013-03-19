package org.cakesolutions.akkapatterns.api

import akka.actor.Actor
import spray._
import routing._
import org.cakesolutions.akkapatterns.core.CoreActorRefs
import akka.util.Timeout

class Api extends Actor with HttpServiceActor
  with CoreActorRefs
  with FailureHandling
  with EndpointMarshalling
  with DefaultAuthenticationDirectives
  with CustomerService
  with HomeService
  with UserService
  {

    // used by the Akka ask pattern
    implicit val timeout = Timeout(10000)

    // lets the CoreActorRef find the actor system used by Spray
    // (this could potentially be a separate system)
    def system = actorSystem

    val routes =
      customerRoute ~
      homeRoute ~
      userRoute

    def receive = runRoute (
      handleRejections(rejectionHandler)(
        handleExceptions(exceptionHandler)(
          routes
        )
      )
    )

}
