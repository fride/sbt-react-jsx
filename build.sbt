name := "jsx-sbt"

version := "0.0.1-SNAPSHOT"

organization := "org.gutencode"

sbtPlugin := true

scriptedSettings

scriptedLaunchOpts += ("-Dproject.version=" + version.value)

libraryDependencies ++= Seq(
   "org.webjars" % "react" % "0.9.0"
)

