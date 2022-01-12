package com.github.polomarcus.utils

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object SparkService {

  def getAndConfigureSparkSession() = {
    SparkSession
      .builder()
      .appName("TelevisionNewsAnalyser")
      .master("local[*]")
      .getOrCreate()
  }

  def getSparkContext() = {
    SparkSession
      .builder()
      .getOrCreate()
  }
}
