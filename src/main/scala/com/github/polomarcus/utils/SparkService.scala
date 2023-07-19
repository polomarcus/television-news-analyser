package com.github.polomarcus.utils

import org.apache.spark.sql.SparkSession

object SparkService {

  def getAndConfigureSparkSession() = {
    val spark = SparkSession
      .builder()
      .appName("TelevisionNewsAnalyser")
      .master("local[*]")
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    spark
  }

  def getSparkContext() = {
    SparkSession
      .builder()
      .getOrCreate()
  }
}
