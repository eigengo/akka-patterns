package org.cakesolutions.akkapatterns.core

import akka.actor.ActorSystem


trait CoreActorRefs {

  def system: ActorSystem

  def applicationActor = system.actorFor("/user/application")
  def userActor = system.actorFor("/user/application/user")
  def loginActor = system.actorFor("/user/application/authentication/login")

}
