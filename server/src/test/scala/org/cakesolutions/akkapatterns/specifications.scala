package org.cakesolutions.akkapatterns

import akka.testkit.TestKit
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

/**
 * @author janmachacek
 */
trait Timers {

  def timed[U](f: => U): Long = {
    val startTime = System.currentTimeMillis
    f
    System.currentTimeMillis - startTime
  }

}

class ConfiguredTestKit extends TestKit(ActorSystem("Test", ConfigFactory.load("server.conf")))