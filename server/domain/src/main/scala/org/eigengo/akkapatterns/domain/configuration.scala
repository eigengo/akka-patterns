package org.eigengo.akkapatterns.domain

import collection.mutable
import reflect.{ClassTag, classTag}
import io.{Codec, Source}
import org.springframework.core.io.DefaultResourceLoader
import java.io.InputStream

/**
 * Stores the configuration
 */
private object ConfigurationStore {
  val entries = mutable.Map[String, AnyRef]()

  def put(key: String, value: AnyRef) {
    entries += ((key, value))
  }

  def get[A: ClassTag] = {
    val erasure = classTag[A].runtimeClass
    entries.values.find(x => erasure.isAssignableFrom(x.getClass)) match {
      case Some(v) => Some(v.asInstanceOf[A])
      case None => None
    }
  }
}

trait Configured {

  def configured[A: Manifest, U](f: A => U) = f(ConfigurationStore.get[A].get)

  def configured[A: Manifest] = ConfigurationStore.get[A].get

}

trait Configuration {

  final def configure[A <: AnyRef](tag: String)(f: => A) = {
    val a = f
    ConfigurationStore.put(tag, a)
    a
  }

  final def configure[A <: AnyRef](f: => A) = {
    val a = f
    ConfigurationStore.put(a.getClass.getName, a)
    a
  }

}

trait Resources {

  protected def readResource(resource: String) = new DefaultResourceLoader().getResource(resource).getInputStream

  protected implicit class StreamString(stream: InputStream) {
	  @deprecated("use Scala IO", "HEAD")
    def mkString =
      try Source.fromInputStream(stream)(Codec.UTF8).mkString
      finally stream.close()
  }

//  PathMatchingResourcePatternResolver

}

