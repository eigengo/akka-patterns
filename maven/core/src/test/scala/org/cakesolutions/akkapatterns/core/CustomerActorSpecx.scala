package org.cakesolutions.akkapatterns.core

import application.{Insert, FindAll, Get, CustomerActor}
import org.cakesolutions.akkapatterns.test.{SpecConfiguration, DefaultTestData}
import org.specs2.mutable.Specification
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.cakesolutions.akkapatterns.domain.Customer
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

/**
 * @author janmachacek
 */
@RunWith(classOf[JUnitRunner])
class CustomerActorSpecx extends TestKit(ActorSystem()) with Specification with SpecConfiguration with DefaultTestData with ImplicitSender {
  val customerActor = TestActorRef[CustomerActor]

  "Getting a known customer works" in {
    customerActor ! Get(janMachacek.id)
    expectMsg(Some(janMachacek))

    success
  }

  "Finding all customers includes jan" in {
    customerActor ! FindAll()
    val customers = expectMsgType[List[Customer]]

    customers must contain (janMachacek)
  }

  "Inserting a new customer must then find it" in {
    customerActor ! Get(joeBloggs.id)
    expectMsg(None)

    customerActor ! Insert(joeBloggs)
    val insertedJoeBloggs = expectMsgType[Customer]

    customerActor ! Get(insertedJoeBloggs.id)
    val loadedJoeBloggs = expectMsgType[Option[Customer]].get

    loadedJoeBloggs.firstName must_== insertedJoeBloggs.firstName
  }

}
