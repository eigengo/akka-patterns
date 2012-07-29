package org.cakesolutions.akkapatterns.api

import cc.spray.http.HttpMethods._
import cc.spray.http._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class HomeServiceSpec extends DefaultApiSpecification {

  "root URL shows the System version" in {
    testRoot(HttpRequest(GET, "/"))(rootService).response.content.as[SystemInfo] match {
      case Right(info) => success
      case Left(failure) => anError
    }
  }

}
