package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService.waitFuture
import com.github.polomarcus.utils.{DateService, FutureService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future

// For implicit conversions from RDDs to DataFrames

object ParserTF1 {
  val logger = Logger(this.getClass)
  val browser = JsoupBrowser()
  implicit val ec = FutureService.ec

  def parseTF1HomeHelper(url: String, defaultUrl : String = "https://www.francetvinfo.fr"): List[News] = {
    val doc = browser.get(url)
    val allTelevisionNews = doc >> elementList("grid-blk__list a") >> attr("href")

    logger.debug(s"""
      I got ${allTelevisionNews.length} days of news
    """)

    val parsedTelevisionNews = allTelevisionNews.map(televisionNewsForOneDay => {
      logger.info(s"Parsing this day of news : $televisionNewsForOneDay")
      parseTF1News(televisionNewsForOneDay, defaultUrl)
    })

    waitFuture[Option[News]](parsedTelevisionNews).flatten
  }
  def parseTF1Home(url: String, defaultUrl : String = "https://www.francetvinfo.fr") = {
    logger.debug("France 2 Url: " + url)

    try {
      parseTF1HomeHelper(url,defaultUrl)
    } catch {
      case e: Exception =>
        logger.info(e.toString)
        try {
          //Try a second time in case of timeout
          Thread.sleep(2000L)
          parseTF1HomeHelper(url,defaultUrl)
        } catch {
          case e: Exception => {
            logger.warn(s"Second exception in a row for, giving up $url " + e.toString)
            Nil
          }
        }
    }
  }

  def parseTF1News(url: String, defaultUrl : String = "https://www.francetvinfo.fr"): Future[List[Option[News]]] = {
    Future {
      try {
        val tvNewsURL = defaultUrl + url
        logger.debug("France 2 Url: " + tvNewsURL)

        val doc = browser.get(tvNewsURL)
        val news = doc >> elementList(".subjects-list li")
        val publishedDate = doc >> text(".schedule span:nth-of-type(1)") // Diffusé le 08/01/2022
        val presenter = doc >> text(".presenter .by")

        logger.info(
          s"""
            This is what i got for this day $url:
            number of news: ${news.length}
          """)

        news.map(x => {
          val title = x >> text(".grid-blk__item-title")
          val linkToDescription = x >> element(".grid-blk__item-lnk") >> attr("href")
          val (description, authors, editor, editorDeputy) = parseDescriptionAuthors(linkToDescription)
          val publishedDate = x >> element("time") >> attr("datetime")

          logger.debug(
            s"""
              I got a news for date : $publishedDate
              title: $title
              link to description : $linkToDescription
              description (30 first char): ${description.take(30)}
            """)

          Some(
            News(title,
              description,
              DateService.getTimestampTF1(publishedDate),
              0, // no order for TF1
              presenter,
              authors,
              editor,
              editorDeputy,
              defaultUrl + linkToDescription,
              tvNewsURL,
              containsWordGlobalWarming(title + description)
          ))
        })
      } catch {
        case e: Exception => {
          logger.error(s"Error parsing this date $defaultUrl $url " + e.toString)
          Nil
        }
      }
    }
  }

  def containsWordGlobalWarming(description: String) : Boolean = {
    description.toLowerCase().contains("réchauffement climatique")
  }

  def parseDescriptionAuthors(url: String, defaultTF1URL : String =  "https://www.francetvinfo.fr") = {
    try {
      val doc = browser.get(defaultTF1URL + url)
      val subTitle = doc >?> text(".article-chapo")
      val description = doc >?> elementList(".paragraph-block")
      val descriptionTotal = description.foldLeft(""){ (acc, paragraph) =>
        val sum = acc + " " + (paragraph >> text("p"))
        logger.debug(s"""
         parseDescriptionAuthors from $paragraph:
        """)

        sum
      }))
      val authors = doc >?> text(".article__author__name")

      val (editor, editorDeputy) = ("", "")

      logger.debug(s"""
      parseDescriptionAuthors from $url:
        $authors
        $editor
        $editorDeputy
        $subTitle
        $descriptionTotal
      """)

      (descriptionTotal + " " + subTitle, authors.getOrElse("").split(", ").toList, editor, editorDeputy.split(", ").toList)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject $defaultTF1URL + $url " + e.toString)
        ("", Nil, "", Nil)
      }
    }
  }
}
