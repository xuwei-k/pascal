name := "pascal"
organization := "com.github.tomasmikula"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
homepage := Some(url("http://github.com/TomasMikula/pascal"))

scalaVersion := "2.11.12"
crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.7", "2.13.0-M2")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.5")

libraryDependencies ++= Seq(
  scalaOrganization.value % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" => scala210ExtraDeps
  case _      => Nil
})

def scala210ExtraDeps = Seq(
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  "org.scalamacros" %% "quasiquotes" % "2.1.0"
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-feature",
  "-language:higherKinds",
  "-deprecation",
  "-unchecked"
)

List(Compile, Test) flatMap { config =>
  Seq(
    // Notice this is :=, not += - all the warning/lint options are simply
    // impediments in the repl.
    scalacOptions in console in config := Seq(
      "-language:_",
      "-Xplugin:" + (packageBin in Compile).value
    )
  )
}

scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}

scalacOptions in Test += "-Yrangepos"

fork in Test := true


/******************
 *** Publishing ***
 ******************/

import ReleaseTransformations._

releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishArtifact in Test := false
publishMavenStyle := true
pomIncludeRepository := Function.const(false)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>git@github.com:TomasMikula/pascal.git</url>
    <connection>scm:git:git@github.com:TomasMikula/pascal.git</connection>
  </scm>
  <developers>
    <developer>
      <id>TomasMikula</id>
      <name>Tomas Mikula</name>
      <url>http://github.com/TomasMikula/</url>
    </developer>
  </developers>
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges)
