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

    val newsDF = StorageService.read("./tv-2013-2021-json/")

    newsDF.show();

    newsDF.printSchema()

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
