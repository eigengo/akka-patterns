import sbtrelease._

/** Project */
name := "akka-patterns"

version := "1.0"

organization := "org.cakesolutions"

scalaVersion := "2.10.0-RC1"

/** Shell */
shellPrompt := { state => System.getProperty("user.name") + "> " }

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

/** Dependencies */
resolvers += "snapshots-repo" at "http://scala-tools.org/repo-snapshots"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies <<= scalaVersion { scala_version => 
	val sprayVersion = "1.1-M4"
	val akkaVersion  = "2.1.0-RC1"
	Seq(
		"com.typesafe.akka" % "akka-kernel_2.10.0-RC1"  % akkaVersion,
		"io.spray" % "spray-can" % sprayVersion,
		"io.spray" % "spray-routing" % sprayVersion,
		"io.spray" % "spray-httpx" % sprayVersion,
		"io.spray" % "spray-util" % sprayVersion,
		"io.spray" % "spray-json" % "1.2.2" cross CrossVersion.full
	)
}

/** Compilation */
javacOptions ++= Seq("-Xmx1812m", "-Xms512m", "-Xss6m")

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

