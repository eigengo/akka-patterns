package org.cakesolutions.akkapatterns.core

import akka.actor.ActorContext
import collection.mutable

private object ConfigurationStore {
  val entries = mutable.Map[String, AnyRef]()

  def put(key: String, value: AnyRef) {
    entries += ((key, value))
  }

  def get[A] = {
    entries.values.find(_.isInstanceOf[A]) match {
      case Some(v) => v.asInstanceOf[A]
      case None => throw new Exception("Cannot find")
    }
  }
}

trait Configured {

  def configured[A](implicit actorContext: ActorContext): A =
    ConfigurationStore.get[A]


}

trait Configurable {

  def configure[R](f: => R) = {

  }

}