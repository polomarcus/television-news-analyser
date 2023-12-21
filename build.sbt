name := "TelevisionNewsAnalyser"
organization := "com.github.polomarcus"

version := "1.0"
scalaVersion := "2.13.12"

fork := true
outputStrategy := Some(StdoutOutput)


val circeVersion = "0.14.6"
val scalaTest = "3.2.15"
val sparkVersion = "3.5.0"
val logback = "1.4.7"
val scalaLogging = "3.9.5"

scalacOptions := Seq("-Xexperimental", "-unchecked", "-deprecation")

// Scrapper
libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "3.0.0"

// HTTP Client
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"

// JSON
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser").map(_ % circeVersion)

// Log
libraryDependencies += "ch.qos.logback" % "logback-classic" % logback
libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "2.0.5"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % scalaLogging

// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.12.5"

// Test
libraryDependencies += "org.scalatest" %% "scalatest" % scalaTest % "test"

//Spark (csv writer)
libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.3.4"

libraryDependencies += ("org.apache.spark" %% "spark-core" % sparkVersion)
  .exclude("log4j", "log4j")
  .exclude("org.slf4j", "slf4j-log4j12")
  .exclude("org.slf4j", "log4j")



libraryDependencies += ("org.apache.spark" %% "spark-sql" % sparkVersion)
  .exclude("org.slf4j", "log4j")
  .exclude("org.slf4j", "slf4j-log4j12")
  .exclude("org.slf4j", "log4j")

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.1"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.15.1"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.15.1"

libraryDependencies += "org.postgresql" % "postgresql" % "42.5.4"
