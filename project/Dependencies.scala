import sbt._
import scala.scalajs.sbtplugin.ScalaJSPlugin._


object Dependencies {

  object Versions {
    val akka            = "2.3.8"
    val akkaStream      = "1.0-M2"
    val akkaHttp        = "1.0-M2"
    val aspectj         = "1.7.4"
    val jawn            = "0.7.2"
    val kamon           = "0.3.5"
    val logback         = "1.1.2"
    val play            = "2.3.7"
    val slick           = "2.1.0"
    val typesafeConfig  = "1.2.0"

    val scalaJSDom      = "0.6"

    val monocle         = "1.0.1"
    val scalaz          = "7.1.0"
    val shapeless       = "2.0.0"
    val spire           = "0.8.2"

    val scalaTest       = "2.2.2"
    val scalaMock       = "3.2"
    val scalaCheck      = "1.12.1"
  }

  val akkaActor       = "com.typesafe.akka"              %%  "akka-actor"                     % Versions.akka
  val akkaContrib     = "com.typesafe.akka"              %%  "akka-contrib"                   % Versions.akka
  val akkaCluster     = "com.typesafe.akka"              %%  "akka-cluster"                   % Versions.akka
  val akkaHttp        = "com.typesafe.akka"              %%  "akka-http-experimental"         % Versions.akkaHttp
  val akkaHttpCore    = "com.typesafe.akka"              %%  "akka-http-core-experimental"    % Versions.akkaHttp
  val akkaHttpTest    = "com.typesafe.akka"              %%  "akka-http-testkit-experimental" % Versions.akkaHttp
  val akkaPersistence = "com.typesafe.akka"              %%  "akka-persistence-experimental"  % Versions.akka
  val akkaSlf4j       = "com.typesafe.akka"              %%  "akka-slf4j"                     % Versions.akka
  val akkaTest        = "com.typesafe.akka"              %%  "akka-testkit"                   % Versions.akka
  val akkaStream      = "com.typesafe.akka"              %%  "akka-stream-experimental"       % Versions.akkaStream
  val aspectJ         = "org.aspectj"                    %   "aspectjrt"                      % Versions.aspectj
  val aspectjWeaver   = "org.aspectj"                    %   "aspectjweaver"                  % Versions.aspectj
  val jawnParser      = "org.spire-math"                 %%  "jawn-parser"                    % Versions.jawn
  val jawnPlay        = "org.spire-math"                 %%  "play-support"                   % Versions.jawn
  val kamon           = "io.kamon"                       %%  "kamon-core"                     % Versions.kamon
  val logback         = "ch.qos.logback"                 %   "logback-classic"                % Versions.logback
  val playJson        = "com.typesafe.play"              %%  "play-json"                      % Versions.play
  val slick           = "com.typesafe.slick"             %%  "slick"                          % Versions.slick
  val slicktest       = "com.typesafe.slick"             %%  "slick-testkit"                  % Versions.slick
  val typesafeConfig  = "com.typesafe"                   %   "config"                         % Versions.typesafeConfig

  val monocleCore     = "com.github.julien-truffaut"     %%  "monocle-core"                   % Versions.monocle
  val monocleGeneric  = "com.github.julien-truffaut"     %%  "monocle-generic"                % Versions.monocle
  val monocleMacro    = "com.github.julien-truffaut"     %%  "monocle-macro"                  % Versions.monocle
  val scalaz          = "org.scalaz"                     %%  "scalaz-core"                    % Versions.scalaz
  val shapeless       = "com.chuusai"                    %%  "shapeless"                      % Versions.shapeless
  val spire           = "org.spire-math"                 %%  "spire"                          % Versions.spire

  val scalaTest       = "org.scalatest"                  %%  "scalatest"                      % Versions.scalaTest
  val scalaMock       = "org.scalamock"                  %%  "scalamock-scalatest-support"    % Versions.scalaMock
  val scalaCheck      = "org.scalacheck"                 %%  "scalacheck"                     % Versions.scalaCheck

  val scalaJSDom = Def.setting(Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % Versions.scalaJSDom
  ))

  def compile    (modules: ModuleID*): Seq[ModuleID] = modules map (_ % "compile")
  def provided   (modules: ModuleID*): Seq[ModuleID] = modules map (_ % "provided")
  def test       (modules: ModuleID*): Seq[ModuleID] = modules map (_ % "test")
  def runtime    (modules: ModuleID*): Seq[ModuleID] = modules map (_ % "runtime")
  def container  (modules: ModuleID*): Seq[ModuleID] = modules map (_ % "container")
}
