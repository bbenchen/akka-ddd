import sbt._
import sbt.Keys._


object Projects extends Build {
  import play.twirl.sbt.SbtTwirl
  import Settings._
  import Unidoc.{settings => unidocSettings}
  import Assembly.{settings => assemblySettings}
  import Package.{serverSettings => packageServerSettings}
  import Release.{settings => releaseSettings}
  import Docker.{settings => dockerSettings}
  import AspectJ.{settings => aspectJSettings}
  import ScalaJs.{settings => scalaJsSettings}
  import Dependencies._

  lazy val root = Project(id = Globals.name, base = file("."))
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)
    .aggregate(
      `akka-ddd-core`,
      `akka-ddd-examples`
    )

  lazy val `akka-ddd-macros` = module("macros", basicSettings)
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)
    )

  lazy val `akka-ddd-core` = module("core", basicSettings)
    .settings(unidocSettings: _*)
    .settings(assemblySettings: _*)
    .settings(
      libraryDependencies ++=
        compile(typesafeConfig, akkaActor, akkaContrib, akkaPersistence, akkaSlf4j) ++
        test(scalaTest, scalaCheck, scalaMock, akkaTest)
    ).dependsOn(`akka-ddd-macros`)

  lazy val `akka-ddd-examples` = module("examples", basicSettings)
    .settings(noPublishing: _*)
    .settings(
      libraryDependencies ++=
        compile(typesafeConfig, logback, akkaActor, akkaContrib, akkaPersistence, akkaSlf4j) ++
        test(scalaTest, scalaCheck, scalaMock, akkaTest)
    ).dependsOn(
      `akka-ddd-core` % "test->test;compile->compile"
    )

  def module(name: String, basicSettings: Seq[Setting[_]]): Project = {
    val id = s"${Globals.name}-$name"
    Project(id = id, base = file(id), settings = basicSettings ++ Seq(Keys.name := id))
  }

  val noPublishing: Seq[Setting[_]] = Seq(publish := { }, publishLocal := { }, publishArtifact := false)

}
