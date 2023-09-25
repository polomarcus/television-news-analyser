package com.github.polomarcus.storage

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.{SparkService, TextService}
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SaveMode}

import scala.sys.process._

object StorageService {
  val logger = Logger(this.getClass)
  val spark = SparkService.getAndConfigureSparkSession()
  val sqlContext = spark.sqlContext
  import spark.implicits._

  val pathAggregated = "docs/data-aggregated-news-json"

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

  def readNews(filterCurrentYear: Boolean = false): DataFrame = {
    val news = StorageService.read("./data-news-json/").as[News]

    val newsDF = if (filterCurrentYear) {
      logger.info("Reading news:  Keeping only 365 days of data")
      news.filter((year($"date") === year(current_date())))
    } else {
      news
    }

    newsDF.createOrReplaceTempView("news")

    newsDF.toDF()
  }

  /**
   * to reapply the climate detection rules if it changes in the future
   * @param df
   * @return
   */
  def resetContainsGlobalWarming(df: DataFrame) = {
    df.as[News]
      .map(news => {
        news.copy(
          containsWordGlobalWarming =
            TextService.containsWordGlobalWarming(news.title + news.description))
      })
      .toDF()
  }
  def updateGlobalWarmingNews() = {
    val newsDF = readNews() // create SQL table

    saveAggregateNews()
    saveLatestNews()
    savePercentMedia()
  }

  /**
   * some news can be uploaded multiple times by human mistakes on website
   * use it this way :
    val newsNoDuplicates = removeDuplicates()
    newsNoDuplicates.createOrReplaceTempView("news")
    saveJSON(newsNoDuplicates, "news-no-duplicates")
   */
  def removeDuplicates() = {
    val newsDFWithoutDuplicates = spark.sql(
      """
        |SELECT title,description,date,order,presenter,authors,editor,editorDeputy,url,urlTvNews,containsWordGlobalWarming,media, year, month, day
        |FROM (
        | SELECT title,description,date,order,presenter,authors,editor,editorDeputy,url,urlTvNews,containsWordGlobalWarming,media, year, month, day, row_number() OVER (
        |            PARTITION BY
        |                title,
        |                description,
        |                date
        |            ORDER BY
        |                date
        |        ) AS row_num
        |FROM news) tmp
        |WHERE row_num = 1
      """.stripMargin)

    newsDFWithoutDuplicates
  }
  def getDuplicateNews() = {
    val newsDF = readNews() // create SQL table

    val duplicates = spark.sql("""
        |SELECT title, media, date
        |FROM (
        |SELECT COUNT(*) AS number_of_news, title, description, media, date
        |FROM news
        |GROUP BY title, description, media, date
        |HAVING COUNT(*) > 1
        |ORDER BY 1 DESC) tmp
      """.stripMargin)
    logger.info("Duplicates news - should be empty:")
    duplicates.show(100, false)

    duplicates
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
    val newsSubQuery =
      spark.sql("""
        | SELECT date_format(date, "yyyy-MM") AS date, media, COUNT(*) AS count
        |        FROM news
        |        WHERE containsWordGlobalWarming = TRUE
        |        GROUP BY date_format(date, "yyyy-MM"), media
      """.stripMargin)
    newsSubQuery.createOrReplaceTempView("newstmp")

    val newsSubQuery2 =
      spark.sql("""
        |SELECT date_format(date, "yyyy-MM") AS date,media,COUNT(*) AS totalNews
        |        FROM news
        |        GROUP BY date_format(date, "yyyy-MM"), media
      """.stripMargin)
    newsSubQuery2.createOrReplaceTempView("newstmp2")

    val mediaPercent =
      spark.sql("""
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

    changeFileName(
      s"$pathAggregated/aggPercent.json",
      s"$pathAggregated/aggPercent.json/aggPercent.json")
  }

  def saveLatestNews() = {
    logger.info("News containing global warming :")

    val latestNews = spark.sql(
      """
        |SELECT date_format(date, "dd/MM/yyyy") AS date, date_format(date, "yyyy-MM-dd") AS datecharts, CONCAT(title, " : ", description) AS title, url, urlTvNews, media
        |FROM news
        |WHERE containsWordGlobalWarming = TRUE
        |ORDER BY datecharts DESC
      """.stripMargin)

    latestNews
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(s"$pathAggregated/latest-news.json")

    changeFileName(
      s"$pathAggregated/latest-news.json",
      s"$pathAggregated/latest-news.json/latestNews.json")
  }

  def write(arrayNews: Seq[News], path: String) = {
    val news = spark.sparkContext
      .parallelize(arrayNews)
      .toDF()

    val finalNews = cleanDataBeforeSaving(news)
    saveJSON(finalNews, path)
  }

  def cleanDataBeforeSaving(news: DataFrame): DataFrame = {
    news
      .withColumn("year", year('date))
      .withColumn("month", month('date))
      .withColumn("day", dayofmonth('date))
      .createOrReplaceTempView("news")

    val newsNoDuplicates = resetContainsGlobalWarming(removeDuplicates())

    newsNoDuplicates
      .withColumn("year", year('date))
      .withColumn("month", month('date))
      .withColumn("day", dayofmonth('date))
  }

  def saveJSON(news: DataFrame, path: String): String = {
    news
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("media", "year", "month", "day")
      .json(s"$path-json")

    path
  }

  def saveCSV(news: DataFrame, path: String): String = {
    logger.info("Saving CSV...")

    news
      .repartition(1)
      .drop("authors") // Column `authors` has a data type of array<string>, which is not supported by CSV
      .drop("editorDeputy") // Column `editorDeputy` has a data type of array<string>, which is not supported by CSV
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("year")
      .option("header", "true")
      .option("delimiter", "\t") //tab
      .option("compression", "gzip")
      .csv(s"$path-csv")

    path
  }

  def read(path: String) = {
    spark.read
      .json(path)
      .withColumn("date", to_timestamp(col("date")))
  }
}
