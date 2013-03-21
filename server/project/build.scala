import sbt._
import Keys._
import sbtscalaxb.Plugin._
import ScalaxbKeys._
import net.virtualvoid.sbt.graph.Plugin._
import org.scalastyle.sbt._
import com.typesafe.sbt.SbtStartScript

// to sync this project with IntelliJ, run the sbt-idea plugin with: sbt gen-idea
//
// to set user-specific local properties, just create "~/.sbt/my-settings.sbt", e.g.
// javaOptions += "some cool stuff"
//
// This project allows a local.conf on the classpath (e.g. domain/src/main/resources) to override settings, e.g.
//
// test.db.mongo.hosts { "Sampo.home": 27017 }
// test.db.cassandra.hosts { "Sampo.home": 9160 }
// main.db.mongo.hosts = ${test.db.mongo.hosts}
// main.db.cassandra.hosts = ${test.db.cassandra.hosts}
//
// mkdir -p {domain,core,api,main,test}/src/{main,test}/{java,scala,resources}/org/cakesolutions/akkapatterns
//
// the following were useful for writing this file
// http://www.scala-sbt.org/release/docs/Getting-Started/Multi-Project.html
// https://github.com/sbt/sbt/blob/0.12.2/main/Build.scala
// https://github.com/akka/akka/blob/master/project/AkkaBuild.scala
object PatternsBuild extends Build {

  override val settings = super.settings ++ Seq(
    organization := "org.cakesolutions.patterns",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.10.1"
  )

  lazy val defaultSettings = Defaults.defaultSettings ++ graphSettings ++ Seq(
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-unchecked"),
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),
	// https://github.com/sbt/sbt/issues/702
	javaOptions += "-Djava.util.logging.config.file=logging.properties",
	javaOptions += "-Xmx2G",
	outputStrategy := Some(StdoutOutput),
	fork := true,
	maxErrors := 1,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases"),
      "Spray Releases" at "http://repo.spray.io",
      Resolver.typesafeRepo("snapshots"),
      Resolver.sonatypeRepo("snapshots"),
	  "Jasper Community" at "http://jasperreports.sourceforge.net/maven2"
	  // resolvers += "neo4j repo" at "http://m2.neo4j.org/content/repositories/releases/"  
    ),
    parallelExecution in Test := false
  ) ++ ScctPlugin.instrumentSettings ++ scalaxbSettings ++ ScalastylePlugin.Settings

  def module(dir: String) = Project(id = dir, base = file(dir), settings = defaultSettings)
  import Dependencies._

  // https://github.com/eed3si9n/scalaxb/issues/199
  lazy val domain = module("domain") settings(
    libraryDependencies += java_logging, // will upgrade to scala_logging when released
	libraryDependencies += akka_contrib,
	libraryDependencies += akka,
	libraryDependencies += scala_io_core,
	libraryDependencies += scala_io_file,
	libraryDependencies += scalad,
	libraryDependencies += hector,
	libraryDependencies += spring_core,
	libraryDependencies += specs2 % "test",
    packageName in scalaxb in Compile := "org.cakesolutions.patterns.domain.soap",
    sourceGenerators in Compile <+= scalaxb in Compile
    )

  lazy val test = module("test") dependsOn (domain) settings (
    libraryDependencies += specs2 % "compile",
	libraryDependencies += cassandra_unit,
	libraryDependencies += akka_testkit % "compile"
  )

  lazy val core = module("core") dependsOn(domain, test % "test") settings (
	libraryDependencies += spray_client,
	libraryDependencies += amqp,
	libraryDependencies += rabbitmq,
	libraryDependencies += mail,
	libraryDependencies += neo4j,
	libraryDependencies += scalaz_effect,
	libraryDependencies += jasperreports,
    libraryDependencies += poi
  )

  lazy val api = module("api") dependsOn(core, test % "test") settings(
    libraryDependencies += spray_routing,
    libraryDependencies += spray_testkit % "test"
    )

  lazy val main = module("main") dependsOn(api, test % "test")

  lazy val root = Project(id = "parent", base = file("."), settings = defaultSettings) settings (
  	  ScctPlugin.mergeReportSettings: _*
	) settings (
	  SbtStartScript.startScriptForClassesSettings: _*
  	) settings (
	  mainClass in (Compile, run) := Some("org.cakesolutions.patterns.main.Main")
	) aggregate (
      domain, test, core, api, main
    ) dependsOn (main) // yuck
}

object Dependencies {
  // to help resolve transitive problems, type:
  //   `sbt dependency-graph`
  //   `sbt test:dependency-tree`
  val bad = Seq(
    ExclusionRule(name = "log4j"),
    ExclusionRule(name = "commons-logging"),
    ExclusionRule(organization = "org.slf4j")
  )

  val akka_version = "2.1.2"
  val spray_version = "1.1-M7"

  val java_logging = "com.github.fommil" % "java-logging" % "1.0"
  val scalad = "org.cakesolutions" %% "scalad" % "1.2.0-SNAPSHOT" // https://github.com/janm399/scalad/issues/7
  val akka = "com.typesafe.akka" %% "akka-actor" % akka_version
  val akka_contrib = "com.typesafe.akka" %% "akka-contrib" % akka_version intransitive()// JUL only
  val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % akka_version
  val scalaz_effect = "org.scalaz" %% "scalaz-effect" % "7.0.0-M9"
  val spring_core = "org.springframework" % "spring-core" % "3.1.4.RELEASE" excludeAll (bad: _*)
  // beware Hector 1.1-2 and Guava 14: https://github.com/hector-client/hector/pull/591
  val guava = "com.google.guava" % "guava" % "13.0.1" // includes Cache
  val jsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.1" // undeclared dep of Guava
  val hector = "org.hectorclient" % "hector-core" % "1.1-2" excludeAll (bad: _*)
  val spray_routing = "io.spray" % "spray-routing" % spray_version
  val spray_client = "io.spray" % "spray-client" % spray_version
  val spray_testkit = "io.spray" % "spray-testkit" % spray_version
  val cassandra_unit = "org.cassandraunit" % "cassandra-unit" % "1.1.2.1" excludeAll (bad: _*)
  val specs2 = "org.specs2" %% "specs2" % "1.13"
  val amqp = "com.github.sstone" %% "amqp-client" % "1.1"
  val rabbitmq = "com.rabbitmq" % "amqp-client" % "2.8.1"
  val neo4j = "org.neo4j" % "neo4j" % "1.9.M05"
  val jasperreports = "net.sf.jasperreports" % "jasperreports" % "5.0.1" excludeAll (bad: _*)
  val poi = "org.apache.poi" % "poi" % "3.9"
  val mail = "javax.mail" % "mail" % "1.4.2"
  val scala_io_core = "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"
  val scala_io_file = "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
}