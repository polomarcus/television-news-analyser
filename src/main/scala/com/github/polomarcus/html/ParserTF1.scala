package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService.waitFuture
import com.github.polomarcus.utils.{DateService, FutureService, TextService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future

// For implicit conversions from RDDs to DataFrames

object ParserTF1 {
  val logger = Logger(this.getClass)
  val browser = Getter.getBrowser()
  val TF1 = "TF1"
  implicit val ec = FutureService.ec

  def parseTF1HomeHelper(
      url: String,
      defaultUrl: String = "https://www.tf1info.fr"): List[News] = {
    val parsedTelevisionNews = Future {
      val tvNewsURL = url
      val doc = browser.get(tvNewsURL)
      val news = doc >> elementList(".ReplayList__Content__Article li") // >> attr("href")

      logger.info(s"""
              This is what i got for this page $url:
              number of news: ${news.length}
            """)

      news.map(x => {
        try {
          val title = x >> text(".ReplayItem__Title")
          val linkToDescription = x >> element("a") >> attr("href")
          val (description, authors, editor, editorDeputy) =
            parseDescriptionAuthors(linkToDescription, defaultUrl)
          val publishedDate = x >> text(".ReplayItem__Date") // Publié le 10 décembre 2020 à 20h08

          logger.debug(s"""
                I got a news for date : $publishedDate
                title: $title
                link to description : $linkToDescription
                description (30 first char): ${description.take(30)}
              """)

          Some(
            News(
              title,
              description,
              DateService.getTimestampTF1(publishedDate),
              0, // no order for TF1
              "",
              authors,
              editor,
              editorDeputy,
              defaultUrl + linkToDescription,
              tvNewsURL,
              TextService.containsWordGlobalWarming(title + description),
              TF1))
        } catch {
          case e: Exception => {
            logger.error(s"Parsing $tvNewsURL " + e.toString)
            None
          }
        }
      })
    }

    waitFuture[Option[News]](List(parsedTelevisionNews)).flatten
  }

  def parseTF1Home(url: String, defaultUrl: String = "https://www.tf1info.fr") = {
    logger.debug("TF1 Url: " + url)

    try {
      parseTF1HomeHelper(url, defaultUrl)
    } catch {
      case e: Exception =>
        logger.info(e.toString)
        try {
          //Try a second time in case of timeout
          Thread.sleep(2000L)
          parseTF1HomeHelper(url, defaultUrl)
        } catch {
          case e: Exception => {
            logger.warn(s"Second exception in a row for, giving up $url " + e.toString)
            Nil
          }
        }
    }
  }

  /**
   * parse T F1 | Reportage T. Jarrion, F. Couturon, F. Petit
   * @param authors
   */
  def parseAuthors(authors: String) = {
    val step1 = authors.split(" Reportage ")
    if (step1.length > 1) {
      step1(1).split(", ").toList
    } else {
      List("")
    }
  }

  def parseDescriptionAuthors(url: String, defaultTF1URL: String = "https://www.tf1info.fr") = {
    try {
      logger.debug(s"Parsing news : $defaultTF1URL$url")
      val doc = browser.get(defaultTF1URL + url)

      val description = doc >> attr("content")("meta[name=twitter:description]")
      val (editor, editorDeputy) = ("", "")

      logger.debug(s"""
      parseDescriptionAuthors from $url:
        $editor
        $editorDeputy
        $description
      """)

      (description, Nil, editor, editorDeputy.split(", ").toList)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject $defaultTF1URL$url " + e.toString)
        ("", Nil, "", Nil)
      }
    }
  }
}
