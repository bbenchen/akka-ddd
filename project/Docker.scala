import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._
import sbtdocker.Plugin.DockerKeys._
import sbtdocker.immutable.Dockerfile
import sbtdocker.Plugin._
import sbtdocker.ImageName

object Docker {

  lazy val settings: Seq[Setting[_]] = baseDockerSettings ++ Seq(
    docker <<= docker.dependsOn(com.typesafe.sbt.packager.universal.Keys.stage in Compile),
    imageName in docker := ImageName(
        namespace = Some(Globals.dockerImageNamespace),
        repository = name.value,
        tag = Some("v" + version.value)
      ),
    dockerfile in docker <<= (name, stagingDirectory in Universal) map {
      case (appName, stageDir) =>
        val workingDir = s"/opt/$appName"
        Dockerfile.empty
          .from(Globals.dockerBaseImage)
          .maintainer(Globals.maintainer)
          .add(stageDir, workingDir)
          .run("chmod",  "+x",  s"/opt/$appName/bin/$appName")
          .volume(workingDir)
          // .expose(8080)
          .workDir(workingDir)
          .entryPoint(s"bin/$appName")
    }
  )

}
