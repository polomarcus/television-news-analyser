package com.github.polomarcus.storage

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.SparkService
import com.typesafe.scalalogging.Logger
import org.apache.spark.sql.functions.{col, to_timestamp, year}

object StorageService {
  val logger = Logger(this.getClass)
  val  spark = SparkService.getSparkContext
  val sqlContext = spark.sqlContext
  import spark.implicits._

  def write(arrayNews: Seq[News], path : String) = {
    val news = spark.sparkContext.parallelize(arrayNews)
      .toDF()
      .repartition(1)

    news.show(10, false)
    news.createOrReplaceTempView("news")

    spark.sql(
      """
        |SELECT COUNT(*), containsWordGlobalWarming
        |FROM news
        |GROUP BY containsWordGlobalWarming
      """.stripMargin).show(10, false)

    news
      .withColumn("year", year('date))
      .write
      .partitionBy("year")
      .json(s"$path-json")

    path
  }

  def read(path: String)  = {
    spark.read.json(path).withColumn("date",
      to_timestamp(col("date")))
  }
}
