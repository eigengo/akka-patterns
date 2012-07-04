package org.cakesolutions.akkapatterns.core.application

import org.specs2.mutable.Specification
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Duration
import akka.actor.{Props, ActorSystem}
import org.cakesolutions.akkapatterns.core.Core

/**
 * @author janmachacek
 */
class BombActorSpec extends TestKit(ActorSystem()) with Specification with ImplicitSender with Core {

  "flooding the actor's queue" in {
    val bombActor = system actorFor "user/application/bomb"

    within(Duration("2s")) {
      for (i <- 0 to 100) {
        bombActor ! Bomb()
        expectMsg(Some("boom!"))
      }
    }

    success
  }

  implicit def actorSystem = system
}
