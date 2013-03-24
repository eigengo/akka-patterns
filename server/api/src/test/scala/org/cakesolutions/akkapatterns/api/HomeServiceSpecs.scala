package org.cakesolutions.akkapatterns.api

class HomeServiceSpecs extends ApiSpecs with HomeService {

  implicit val route = homeRoute

  "/" should {
    "return the service info" in {
      Get("/").returnsA[SystemInfo]
    }
  }

}
