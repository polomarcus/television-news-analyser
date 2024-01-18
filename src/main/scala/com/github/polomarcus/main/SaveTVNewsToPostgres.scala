package com.github.polomarcus.main

import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.{DataFrame, SaveMode}

import java.lang.Thread.sleep

object SaveTVNewsToPostgres {
  def main(args: Array[String]) {
    val spark = SparkService.getAndConfigureSparkSession()
    val sqlContext = spark.sqlContext
    val logger = Logger(this.getClass)
    val dbTable = "news_broadcast"

    // Log last file of 2024 to debug scaleway registry
    StorageService.listLastFilesJson()

    val newsDFTmp: DataFrame = StorageService.readNews().toDF()
    val newsDF = StorageService.resetContainsGlobalWarming(newsDFTmp)

    val scaleway = sys.env.getOrElse("SCALEWAY", "false")
    val dbHost = sys.env.getOrElse("POSTGRES_HOST", "postgres")
    val dbPort = sys.env.getOrElse("POSTGRES_PORT", "5432")
    val dbName = sys.env.getOrElse("POSTGRES_DB", "metabase")
    val dbUser = sys.env.getOrElse("POSTGRES_USER", "user")
    val dbPassword = sys.env.getOrElse("POSTGRES_PASSWORD", "password")
    val connectionUrl = s"jdbc:postgresql://$dbHost:$dbPort/$dbName"
    logger.warn(s"Connecting to $connectionUrl")
    // Overwrite new data to a JDBC source
    logger.info(s"Overwriting all previous data with new ones")

    newsDF.write
      .format("jdbc")
      .option("url", connectionUrl)
      .option("driver", "org.postgresql.Driver")
      .option("dbtable", dbTable)
      .option("user", dbUser)
      .option("password", dbPassword)
      .mode(SaveMode.Overwrite)
      .save()

    logger.info(s"Saved news inside PG database in table ${dbTable}")

    // Without this we have a CrashLoopBackOff (Kubernetes health error)
    if (scaleway != "false") {
      val minutesToSleep = 15
      logger.warn(s"Scaleway : Sleeping for ${minutesToSleep} to avoid CrashLoopBackOff")
      sleep(1000 * 60 * minutesToSleep)
    }

    spark.stop()
    System.exit(0)
  }
}
