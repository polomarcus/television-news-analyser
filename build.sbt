name := "TelevisionNewsAnalyser"
organization := "com.github.polomarcus"

version := "1.0"
scalaVersion := "2.12.15"

val circeVersion = "0.10.0"
val scalaTest = "3.2.9"
val sparkVersion = "2.4.5"
val logback = "1.2.3"
val scalaLogging = "3.9.4"

scalacOptions := Seq("-Xexperimental", "-unchecked", "-deprecation")

// Scrapper
libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.1"

// HTTP Client
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

// JSON
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

// Log
libraryDependencies += "ch.qos.logback" % "logback-classic" % logback
libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.32"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.9"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging


// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.10.13"

// Test
libraryDependencies +=  "org.scalatest" %% "scalatest" % scalaTest % "test"