name := "sbt-react-jsx"

version := "0.0.1-SNAPSHOT"

organization := "org.gutencode"

sbtPlugin := true

scriptedSettings

scriptedLaunchOpts += ("-Dproject.version=" + version.value)

libraryDependencies ++= Seq(
   "org.webjars" % "react" % "0.9.0",
   "org.scalacheck" %% "scalacheck" % "1.11.3" % "test",
   "org.specs2" %% "specs2" % "2.3.11" % "test"
)

