name := "TelevisionNewsAnalyser"
organization := "com.github.polomarcus"

version := "1.0"
scalaVersion := "2.12.15"

val circeVersion = "0.10.0"
val scalaTest = "3.2.9"
val sparkVersion = "2.4.8"
val logback = "1.2.10"
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
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging


// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.10.13"

// Test
libraryDependencies +=  "org.scalatest" %% "scalatest" % scalaTest % "test"

//Spark (csv writer)
libraryDependencies += ("org.apache.spark" %% "spark-core" % sparkVersion)
  .exclude("log4j", "log4j")
  .exclude("org.slf4j", "slf4j-log4j12")
  .exclude("org.slf4j", "log4j")

libraryDependencies += ("org.apache.spark" %% "spark-sql" % sparkVersion)
  .exclude("org.slf4j", "log4j")
  .exclude("org.slf4j", "slf4j-log4j12")
  .exclude("org.slf4j", "log4j")

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.7"