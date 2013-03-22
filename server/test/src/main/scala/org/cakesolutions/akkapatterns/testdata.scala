package org.cakesolutions.akkapatterns

import domain._
import org.specs2.mutable.Before
import com.mongodb.{BasicDBList, DBObject, DB}
import com.mongodb.util.JSON
import scala.collection.JavaConversions._
import akka.contrib.jul.JavaLogging
import java.util.UUID

/*
 * The idea with test data is to provide
 *
 * 1. Fixtures - can be attached to specs (individual or for a whole file)
 *               which define the state of the database at the beginning
 *               of the spec examples. Typically these are loaded per example.
 * 2. Identifiers - can be used to lookup data that is inserted into the DB
 *               by the fixtures. Typically these are mixed in to specs.
 * 3. Data Instances - which is suitable for programmatically inserting into
 *               the database. Typically these are mixed in to specs.
 */

object MongoCollectionFixture {
  class Fix(names: String*) extends MongoCollectionFixture(true, names:_*)

  class ContinueFix(names: String*) extends MongoCollectionFixture(false, names:_*)
}

/**
 * Fixture that evaluates named files (in Mongo Javascript format) from the classpath.
 *
 * @param clean if true, will drop the database before running the fixture
 * @param names
 */
class MongoCollectionFixture(clean: Boolean, names: String*) extends Configured with Resources with JavaLogging with Before {
  override def before() {
    if (clean)
      configured[DB].dropDatabase()

    val header = readResource(s"classpath:/org/cakesolutions/akkapatterns/testdata/common.js").mkString
    names.foreach {
      name =>
        configured[DB].eval(
          header +
          readResource(s"classpath:/org/cakesolutions/akkapatterns/testdata/${name}.js").mkString
        )
    }
  }
}


//trait TestUserData {
//  val TestGuestUserId = UUID.fromString("994fc1f0-90a9-11e2-9e96-0800200c9a66")
//  val TestCustomerUserId = UUID.fromString("7370f980-90aa-11e2-9e96-0800200c9a66")
//  val TestAdminUserId = UUID.fromString("c0a93190-90aa-11e2-9e96-0800200c9a66")
//}

trait TestCustomerData {
  val TestCustomerJanId = UUID.fromString("122fa630-92fd-11e2-9e96-0800200c9a66")

}
