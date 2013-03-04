import sbtrelease._

/** Project */
name := "akka-patterns"

version := "1.0"

organization := "org.cakesolutions"

scalaVersion := "2.10.0"

/** Shell */
shellPrompt := { state => System.getProperty("user.name") + "> " }

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

/** Dependencies */
resolvers += "snapshots-repo" at "http://scala-tools.org/repo-snapshots"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "neo4j repo" at "http://m2.neo4j.org/content/repositories/releases/"

resolvers += "neo4j snapshot repo" at "http://m2.neo4j.org/content/groups/public/"

libraryDependencies <<= scalaVersion { scala_version => 
    val sprayVersion = "1.1-20130207"
    val akkaVersion  = "2.1.0"
    val scalazVersion = "7.0.0-M8"
    Seq(
        "com.typesafe.akka"   %% "akka-kernel"         % akkaVersion,
        "com.typesafe.akka"   %% "akka-actor"          % akkaVersion,
        "io.spray"             % "spray-can"           % sprayVersion,
        "io.spray"             % "spray-routing"       % sprayVersion,
        "io.spray"             % "spray-httpx"         % sprayVersion,
        "io.spray"             % "spray-util"          % sprayVersion,
        "io.spray"             % "spray-client"        % sprayVersion,
        "com.aphelia"         %% "amqp-client"         % "1.0",
        "com.rabbitmq"         % "amqp-client"         % "2.8.1",
        "org.neo4j"            % "neo4j"               % "1.9-M02",
        "org.scalaz"          %% "scalaz-effect"       % scalazVersion,
        "org.scalaz"          %% "scalaz-core"         % scalazVersion,
        "org.cakesolutions"    % "scalad_2.10"         % "1.0",
        "io.spray"            %% "spray-json"          % "1.2.3",
        "javax.mail"           % "mail"                % "1.4.2",
        "org.specs2"           % "classycle"           % "1.4.1" % "test",
        "com.typesafe.akka"   %% "akka-testkit"        % akkaVersion % "test",
        "org.specs2"          %% "specs2"              % "1.13" % "test"
    )
}

/** Compilation */
javaOptions += "-Xmx2G"

scalacOptions ++= Seq("-deprecation", "-unchecked")

maxErrors := 20 

pollInterval := 1000

logBuffered := false

cancelable := true

testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "all").exists(s.endsWith(_)) &&
    !s.endsWith("FeaturesSpec") ||
    s.contains("UserGuide") || 
    s.contains("index") ||
    s.matches("org.specs2.guide.*")))

/** Console */
initialCommands in console := "import org.cakesolutions.akkapatterns._"

seq(ScctPlugin.instrumentSettings : _*)
