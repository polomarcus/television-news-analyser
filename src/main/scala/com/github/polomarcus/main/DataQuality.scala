package com.github.polomarcus.main

import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger

object DataQuality {
  def main(args: Array[String]) {
    val logger = Logger(this.getClass)
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext

    val duplicates = StorageService.getDuplicateNews()
    if (duplicates.collect().isEmpty) {
      logger.info("SUCCESS : No duplicates")
      spark.stop()
      System.exit(0)
    } else {
      logger.error("Duplicates found")
      spark.stop()
      System.exit(1)
    }
  }
}
