resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0-SNAPSHOT")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")
