package org.eigengo.akkapatterns.core

import akka.actor.ActorSystem


trait CoreActorRefs {

  def system: ActorSystem

  def coordinatorActor = system.actorFor("/user/recog/coordinator")
//  def applicationActor = system.actorFor("/user/application")
//  def userActor = system.actorFor("/user/application/user")
//  def loginActor = system.actorFor("/user/application/authentication/login")

}
