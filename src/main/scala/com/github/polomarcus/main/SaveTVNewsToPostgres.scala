package com.github.polomarcus.main

import com.github.polomarcus.model.News
import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.{Dataset, SaveMode}

object SaveTVNewsToPostgres {
  def main(args: Array[String]) {
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext
    val logger = Logger(this.getClass)

    val newsFR2DF = StorageService.read("./data-news-json/fr2-tv-news-2013-2022-json").distinct()
    val newsTF1DF = StorageService.read("./data-news-json/tf1-tv-news-2016-2022-json").distinct()

    val newsDF = newsTF1DF.union(newsFR2DF)

    newsDF.printSchema()
    newsDF.show()

    // Saving data to a JDBC source
    newsDF.write
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost:5432/metabase")
      .option("driver", "org.postgresql.Driver")
      .option("dbtable", "news")
      .option("user", "user")
      .option("password", "password")
      .mode(SaveMode.Append)
      .save()

    logger.info("Saved news inside PG database")

    spark.stop()
    System.exit(0)
  }
}
