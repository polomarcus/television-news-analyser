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
    val dbTable = "AA_news"
    val newsDF = StorageService.readNews()
    val dbHost = sys.env.getOrElse("postgres", "localhost")
    logger.warn(s"Connecting to jdbc:postgresql://$dbHost:5432/metabase")
    // Overwrite new data to a JDBC source
    logger.info(s"Overwritting all previous data with new ones")
    newsDF.write
      .format("jdbc")
      .option("url", s"jdbc:postgresql://$dbHost:5432/metabase")
      .option("driver", "org.postgresql.Driver")
      .option("dbtable", dbTable)
      .option("user", "user")
      .option("password", "password")
      .mode(SaveMode.Overwrite)
      .save()

    logger.info("Saved news inside PG database")

    spark.stop()
    System.exit(0)
  }
}
