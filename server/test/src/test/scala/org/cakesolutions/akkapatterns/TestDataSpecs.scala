package org.cakesolutions.akkapatterns

import org.cakesolutions.akkapatterns.domain.{User, UserMongo}
import com.mongodb.DB
import org.cakesolutions.scalad.mongo.sprayjson._
import java.util.UUID

class TestDataSpecs extends NoActorSpecs with CleanMongo with UserMongo {

  "Mongo Test Data" should {
    "be clean at the beginning" in {
        configured[DB].getCollectionNames.size === 0
    }

    "users fixture should attach" in new MongoCollectionFixture("users") with TestUserData {
      mongo.count[User]() must beGreaterThan(0L)
      mongo.searchFirst[User]("id":>TestGuestUserId) must beLike {
        case Some(user) if user.username == "guest" => ok
      }
      mongo.searchFirst[User]("id":>TestCustomerUserId) must beLike {
        case Some(user) if user.username == "customer" => ok
      }
      mongo.searchFirst[User]("id":>TestAdminUserId) must beLike {
        case Some(user) if user.username == "root" => ok
      }
      mongo.searchFirst[User]("id":>UUID.randomUUID()) must beLike {
        case None => ok
      }
    }
  }
}
