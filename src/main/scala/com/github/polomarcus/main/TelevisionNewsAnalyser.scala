package com.github.polomarcus.main

import com.github.polomarcus.html.Getter
import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.{DateService, SparkService}
import com.typesafe.scalalogging.Logger


object TelevisionNewsAnalyser {
  def main(args: Array[String]) {
    val logger = Logger(this.getClass)
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext
    logger.info(s"args $args")

    val firstNews = 1
    val (start, end) = if (args.length == 0) {
      logger.info("Args can must between 0 and 170 (2013 TV news): default is https://www.francetvinfo.fr/replay-jt/france-2/20-heures/")
      (firstNews, 1)
    } else {
      logger.info(s"Getting TV news until page https://www.francetvinfo.fr/replay-jt/france-2/20-heures/${args(0)}.html")
      (firstNews, args(0).toInt)
    }

    // Parsed HTML to extract metadata about news
    val newsList = Getter.getFrance2News(start, end)

    logger.info(s"Number of news parsed : ${newsList.length}")

    //@TODO save as JSON
    StorageService.write(newsList, "myFirstTest")
    spark.stop()
    System.exit(0)
  }
}

