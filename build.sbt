import sbt.Keys._

name := "scalafakeit"
organization := "scalafakeit"

version := "0.0.3-SNAPSHOT"

scalaVersion := "2.12.4"

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "org.scalactic" % "scalactic_2.11" % "2.2.6" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test")

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)
    
