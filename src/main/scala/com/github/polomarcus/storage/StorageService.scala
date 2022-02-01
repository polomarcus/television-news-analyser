package com.github.polomarcus.storage

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.functions.{col, dayofmonth, month, to_timestamp, year}

import scala.sys.process._

object StorageService {
  val logger = Logger(this.getClass)
  val spark = SparkService.getAndConfigureSparkSession()
  val sqlContext = spark.sqlContext
  import spark.implicits._

  val pathAggregated = "website/data-aggregated-news-json"

  /**
   * @see https://alvinalexander.com/scala/how-to-handle-wildcard-characters-running-external-commands/
   * @param source
   * @param destination
   */
  def changeFileName(source: String, destination: String): Unit = {
    val output = Seq("/bin/sh", "-c", s"mv ./$source/*.json ./$destination").!
    val outputCleanCRC = Seq("/bin/sh", "-c", s"rm ./$source/.*.crc").!
    val outputCleanSuccess = Seq("/bin/sh", "-c", s"rm ./$source/_SUCCESS").!
    logger.info(s"Change name of Spark out to be versioned by git : $output")
  }

  def readNews(): DataFrame = {
    val newsDF = StorageService.read("./data-news-json/").distinct()
    newsDF.createOrReplaceTempView("news")
    newsDF.printSchema()
    newsDF.show()

    newsDF
  }

  def updateGlobalWarmingNews() = {
    val newsDF = readNews() // create SQL table
    saveAggregateNews()
    saveLatestNews()
    savePercentMedia()
  }

  def saveAggregateNews() = {
    val media = spark.sql(
      """
        |SELECT COUNT(*) AS number_of_news, containsWordGlobalWarming, media, date_format(date, "yyyy-MM") AS datecharts, date_format(date, "MM/yyyy") AS date
        |FROM news
        |GROUP BY containsWordGlobalWarming, media, 4, 5
        |ORDER BY datecharts ASC
      """.stripMargin)

    media.show(10, false)

    media
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(s"$pathAggregated/agg.json")

    changeFileName(s"$pathAggregated/agg.json", s"$pathAggregated/agg.json/agg.json")
  }

  def savePercentMedia() = {
    val newsSubQuery = spark.sql(
      """
        | SELECT date_format(date, "yyyy-MM") AS date, media, COUNT(*) AS count
        |        FROM news
        |        WHERE containsWordGlobalWarming = TRUE
        |        GROUP BY date_format(date, "yyyy-MM"), media
      """.stripMargin)
    newsSubQuery.createOrReplaceTempView("newstmp")

    val newsSubQuery2 = spark.sql(
      """
        |SELECT date_format(date, "yyyy-MM") AS date,media,COUNT(*) AS totalNews
        |        FROM news
        |        GROUP BY date_format(date, "yyyy-MM"), media
      """.stripMargin)
    newsSubQuery2.createOrReplaceTempView("newstmp2")

    val mediaPercent = spark.sql(
      """
        |SELECT ROUND(count * 100.0 /totalNews, 2) AS percent, newstmp.media, newstmp.date
        |FROM newstmp
        |JOIN newstmp2
        |ON newstmp.date = newstmp2.date AND newstmp.media = newstmp2.media
        |ORDER BY newstmp.date DESC, newstmp.media ASC
      """.stripMargin)

    mediaPercent.show(10)

    mediaPercent
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(s"$pathAggregated/aggPercent.json")

    changeFileName(s"$pathAggregated/aggPercent.json", s"$pathAggregated/aggPercent.json/aggPercent.json")
  }

  def saveLatestNews() = {
    logger.info("News containing global warming :")

    val latestNews = spark.sql(
      """
        |SELECT date_format(date, "dd/MM/yyyy") AS date, date_format(date, "yyyy-MM-dd") AS datecharts,title, url, urlTvNews, media
        |FROM news
        |WHERE containsWordGlobalWarming = TRUE
        |ORDER BY datecharts DESC
      """.stripMargin)

    latestNews
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(s"$pathAggregated/latest-news.json")

    changeFileName(s"$pathAggregated/latest-news.json", s"$pathAggregated/latest-news.json/latestNews.json")
  }

  def write(arrayNews: Seq[News], path : String) = {
    val news = spark.sparkContext.parallelize(arrayNews)
      .toDF()
      .repartition(1)

    news.printSchema()

    news.createOrReplaceTempView("news")

    val latestNews = spark.sql(
      """
        |SELECT date_format(date, "dd/MM/yyyy") AS date, title, url, urlTvNews, media
        |FROM news
        |WHERE containsWordGlobalWarming = TRUE
        |ORDER BY date ASC
      """.stripMargin)

    latestNews.show(100, false)

    news
      .withColumn("year", year('date))
      .withColumn("month", month('date))
      .withColumn("day", dayofmonth('date))
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("media","year", "month","day")
      .json(s"$path-json")

    path
  }

  def read(path: String)  = {
    spark.read.json(path).withColumn("date",
      to_timestamp(col("date")))
  }
}
