package com.github.polomarcus.main

import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger

object SaveCSV {
  def main(args: Array[String]) {
    val logger = Logger(this.getClass)
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext


    val news =
      StorageService.cleanDataBeforeSaving(
        StorageService.readNews(filterCurrentYear = false).toDF()
      )

   StorageService.saveCSV(news, "data-news")
   //@TODO change limit once approved
    StorageService.saveTSVGarganText(news, "data-news")
    logger.info("saveCSV & saveTSVGarganText done")
  }
}
