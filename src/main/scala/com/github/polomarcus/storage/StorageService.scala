package com.github.polomarcus.storage

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions.{col, dayofmonth, month, to_timestamp, year}

object StorageService {
  val logger = Logger(this.getClass)
  val  spark = SparkService.getSparkContext
  val sqlContext = spark.sqlContext
  import spark.implicits._

  def write(arrayNews: Seq[News], path : String) = {
    val news = spark.sparkContext.parallelize(arrayNews)
      .toDF()
      .repartition(1)

    news.printSchema()

    news.createOrReplaceTempView("news")

    val media = spark.sql(
      """
        |SELECT COUNT(*) AS number_of_news, containsWordGlobalWarming, media
        |FROM news
        |GROUP BY containsWordGlobalWarming, media
      """.stripMargin)

    media.show(10, false)

    logger.info("News containing global warming :")

    val latestNews = spark.sql(
      """
        |SELECT date, url, urlTvNews, media
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
