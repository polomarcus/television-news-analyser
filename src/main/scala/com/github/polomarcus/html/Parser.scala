package com.github.polomarcus.html

import com.github.polomarcus.model.News

import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import com.github.polomarcus.utils.{DateService, FutureService}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{Await, Future}

// For implicit conversions from RDDs to DataFrames
import scala.concurrent.ExecutionContext

object Parser {
  val logger = Logger(this.getClass)
  implicit val ec = FutureService.ec

  def parseFrance2News(url: String) = {
    Future {
      logger.info("France 2 Url: " + url)
      val doc = Getter.browser.get(url)
      val news = doc >> elementList(".subjects-list li")
      val publishedDate = doc >> element(".publishDate time") >> attr("value")("datetime")

      logger.info(s"""
      This is what i got :
      news: ${news.length}
      date : $publishedDate
      """)

      news.map(x => {
        val title = x >> text(".title")
        val order = x >> text(".number")
        val linkToDescription = x >> element(".title")  >> attr("href")
        logger.info("linkToDescription", linkToDescription)
        val description = parseDescription(linkToDescription)

        logger.info(s"""
        I got a news in order $order :
        news: $title
        link to description : $linkToDescription
        description (10 first char: ${description.take(10)}
        """)

        News(title, description, DateService.getTimestamp(publishedDate), order.toInt)
      })
    }
  }

  def parseDescription(url: String) = {
    val defaultFrance2URL =  "https://www.francetvinfo.fr"

    val doc = Getter.browser.get(defaultFrance2URL + url)
    val description = doc >> text(".c-body")
    logger.debug("parseDescription ", description)

    description
  }
}
