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
    logger.info(s"""
                    Args are :
                    * max pagination to scrap from TF1 or France 2 website (integer - 156) - default : 1
                    * media : tf1 or france2 (default)
                yours args : $args
                """)

    val firstNews = 1
    val (start, end, media) = if (args.length == 0) {
      logger.warn(
        "Args must between 0 and 170 (2013 TV news): default is https://www.francetvinfo.fr/replay-jt/france-2/20-heures/")
      (firstNews, 1, "france2")
    } else if (args.length == 2) {
      logger.info(s"Getting TV news until page ${args(0)}")
      (firstNews, args(0).toInt, args(1).toLowerCase())
    } else {
      logger.warn("too many arguments - parsing France 2 website first page")
      (firstNews, 1, "france2")
    }

    // Parsed HTML to extract metadata about news
    val newsList = Getter.getNews(start, end, media)
    if (newsList.isEmpty) {
      logger.error(s"No news parsed for $media")
      spark.stop()
      System.exit(1)
    } else {
      logger.info(s"Number of news parsed : ${newsList.length}")
      StorageService.write(newsList, s"output-${media}-tv-news")

      spark.stop()
      System.exit(0)
    }
  }
}
