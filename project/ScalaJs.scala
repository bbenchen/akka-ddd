import sbt._
import sbt.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin.scalaJSSettings


object ScalaJs {

  lazy val settings: Seq[Setting[_]] = scalaJSSettings

}
