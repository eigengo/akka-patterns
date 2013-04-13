package org.eigengo.akkapatterns

import org.specs2.specification.Analysis
import org.specs2.analysis.ClassycleDependencyFinder
import com.mongodb.DB

class ArchitectureSpec extends NoActorSpecs with Analysis with ClassycleDependencyFinder with TestMongo {

  "The architecture" should {
    "Have properly defined layers" in {
      val ls = layers(
        "main",
        "web",
        "api",
        "core",
        "domain"
      ).withPrefix("org.eigengo.akkapatterns").inTargetDir("target/scala-2.10")

      ls must beRespected
    }
  }

  "The mongo database" should {
    "be configured" in {
      configured[DB] must not beNull
    }
  }

}
