package org.cakesolutions.akkapatterns.test

import org.cakesolutions.akkapatterns.domain.Configuration
import io.Source
import com.mongodb.casbah.MongoConnection

/**
 * @author janmachacek
 */
trait SpecConfiguration extends Configuration {

  configure {
    val mongoDb = MongoConnection()("akka-patterns-test")
    val json = Source.fromInputStream(classOf[SpecConfiguration].getResourceAsStream("/org/cakesolutions/akkapatterns/mongodb-base.js")).mkString
    mongoDb.eval(json)

    mongoDb
  }

}
