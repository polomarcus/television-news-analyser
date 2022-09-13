package com.github.polomarcus.main

import com.github.polomarcus.html.Getter
import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger

object UpdateNews {
  def main(args: Array[String]) {
    val logger = Logger(this.getClass)
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext

    StorageService.updateGlobalWarmingNews()

    spark.stop()
    System.exit(0)
  }
}
