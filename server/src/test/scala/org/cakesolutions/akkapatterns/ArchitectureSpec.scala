package org.cakesolutions.akkapatterns

import org.specs2.mutable.Specification
import org.specs2.specification.Analysis
import org.specs2.analysis.ClassycleDependencyFinder

class ArchitectureSpec extends Specification with Analysis with ClassycleDependencyFinder {

  "The architecture" should {
    "Have properly defined layers" in {
      val ls = layers(
        "main",
        "web",
        "api",
        "core",
        "domain"
      ).withPrefix("org.cakesolutions.akkapatterns").inTargetDir("target/scala-2.10")

      ls must beRespected
    }
  }

}
