import BuildSettings._
import Bundle._
import ConfigFiles._
import CopyLogback._
import Dependencies._
import VersionFile._
import sbt.Keys._
import sbt._

// Root project

lazy val root = Project("gatling-parent", file("."))
  .enablePlugins(AutomateHeaderPlugin, SonatypeReleasePlugin)
  .dependsOn(Seq(commons, core, http, jms, jdbc, redis).map(_ % "compile->compile;test->test"): _*)
  .aggregate(commons, core, jdbc, redis, httpAhc, http, jms, charts, metrics, app, recorder, testFramework, bundle, compiler)
  .settings(basicSettings: _*)
  .settings(noArtifactToPublish)
  .settings(libraryDependencies ++= docDependencies)
  .settings(updateOptions := updateOptions.value.withGigahorse(false))

// Modules

def gatlingModule(id: String) = Project(id, file(id))
  .enablePlugins(AutomateHeaderPlugin, SonatypeReleasePlugin)
  .settings(gatlingModuleSettings: _*)

lazy val commons = gatlingModule("gatling-commons")
  .settings(libraryDependencies ++= commonsDependencies(scalaVersion.value))

lazy val core = gatlingModule("gatling-core")
  .dependsOn(commons % "compile->compile;test->test")
  .settings(libraryDependencies ++= coreDependencies)
  .settings(generateVersionFileSettings: _*)
  .settings(copyGatlingDefaults(compiler): _*)

lazy val jdbc = gatlingModule("gatling-jdbc")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= jdbcDependencies)

lazy val redis = gatlingModule("gatling-redis")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= redisDependencies)

lazy val httpAhc = gatlingModule("gatling-http-ahc")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= httpAhcDependencies)

lazy val http = gatlingModule("gatling-http")
  .dependsOn(httpAhc % "compile->compile;test->test")
  .settings(libraryDependencies ++= httpDependencies)

lazy val jms = gatlingModule("gatling-jms")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= jmsDependencies)
  .settings(parallelExecution in Test := false)

lazy val charts = gatlingModule("gatling-charts")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= chartsDependencies)
  .settings(excludeDummyComponentLibrary: _*)
  .settings(chartTestsSettings: _*)

lazy val metrics = gatlingModule("gatling-metrics")
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= metricsDependencies)

lazy val compiler = gatlingModule("gatling-compiler")
  .settings(libraryDependencies ++= compilerDependencies(scalaVersion.value))

lazy val benchmarks = gatlingModule("gatling-benchmarks")
  .dependsOn(core, http)
  .enablePlugins(JmhPlugin)
  .settings(libraryDependencies ++= benchmarkDependencies)

lazy val app = gatlingModule("gatling-app")
  .dependsOn(core, http, jms, jdbc, redis, metrics, charts)

lazy val recorder = gatlingModule("gatling-recorder")
  .dependsOn(core % "compile->compile;test->test", http)
  .settings(libraryDependencies ++= recorderDependencies)

lazy val testFramework = gatlingModule("gatling-test-framework")
  .dependsOn(app)
  .settings(libraryDependencies ++= testFrameworkDependencies)

lazy val bundle = gatlingModule("gatling-bundle")
  .dependsOn(core, http)
  .enablePlugins(UniversalPlugin)
  .settings(generateConfigFiles(core): _*)
  .settings(generateConfigFiles(recorder): _*)
  .settings(copyLogbackXml(core): _*)
  .settings(bundleSettings: _*)
  .settings(noArtifactToPublish)
