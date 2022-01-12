package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService.waitFuture

import java.util.concurrent.Executors
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import com.github.polomarcus.utils.{DateService, FutureService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.{Future}

// For implicit conversions from RDDs to DataFrames
import scala.concurrent.ExecutionContext

object Parser {
  val logger = Logger(this.getClass)
  val browser = JsoupBrowser()
  implicit val ec = FutureService.ec

  def parseFrance2HomeHelper(url: String, defaultUrl : String = "https://www.francetvinfo.fr"): List[News] = {
    val doc = browser.get(url)
    val allTelevisionNews = doc >> elementList("h2.title a") >> attr("href")

    logger.debug(s"""
      I got ${allTelevisionNews.length} days of news
    """)

    val parsedTelevisionNews = allTelevisionNews.map(televisionNewsForOneDay => {
      logger.info(s"Parsing this day of news : $televisionNewsForOneDay")
      parseFrance2News(televisionNewsForOneDay, defaultUrl)
    })

    waitFuture[Option[News]](parsedTelevisionNews).flatten
  }
  def parseFrance2Home(url: String, defaultUrl : String = "https://www.francetvinfo.fr") = {
    logger.debug("France 2 Url: " + url)

    try {
      parseFrance2HomeHelper(url,defaultUrl)
    } catch {
      case e: Exception =>
        logger.info(e.toString)
        try {
          //Try a second time in case of timeout
          Thread.sleep(2000L)
          parseFrance2HomeHelper(url,defaultUrl)
        } catch {
          case e: Exception => {
            logger.warn(s"Second exception in a row for, giving up $url " + e.toString)
            Nil
          }
        }
    }
  }

  def parseFrance2News(url: String, defaultUrl : String = "https://www.francetvinfo.fr"): Future[List[Option[News]]] = {
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
            date : $publishedDate
            getTimestampFrance2 : ${DateService.getTimestampFrance2(publishedDate)}
          """)

        news.map(x => {
          val title = x >> text(".title")
          val order = x >> text(".number")
          val linkToDescription = x >> element(".title") >> attr("href")
          val (description, authors, editor, editorDeputy) = parseDescriptionAuthors(linkToDescription)

          logger.debug(
            s"""
              I got a news in order $order :
              title: $title
              link to description : $linkToDescription
              description (30 first char): ${description.take(30)}
            """)

          Some(
            News(title,
              description,
              DateService.getTimestampFrance2(publishedDate),
              order.toInt,
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

  def parseDescriptionAuthors(url: String, defaultFrance2URL : String =  "https://www.francetvinfo.fr") = {
    try {
      val doc = browser.get(defaultFrance2URL + url)
      val descriptionOption = doc >?> text(".c-body")
      val description = descriptionOption match {
        case Some(descriptionValue) => descriptionValue
        case None => {
          val oldDescription = (doc >?> text("#col-middle")).getOrElse("")
          oldDescription.split("Le JT")(0) // hack to proper extract description from old pages @see https://www.francetvinfo.fr/attaque-chimique-en-syrie-quelles-consequences_397173.html
        } // old tv news
      }
      val authors = doc >?> text(".c-signature__names span")

      val weekTeam = (doc >> elementList(".from-same-show__info-team"))

      val (editor, editorDeputy) = weekTeam.isEmpty match {
        case false => val editor = (weekTeam.head >?> text("p:nth-of-type(2)")).getOrElse("")
          val editorDeputy = (weekTeam.tail.head >?> text("p:nth-of-type(2)")).getOrElse("")
          (editor, editorDeputy)
        case true => ("", "")
      }

      logger.debug(s"""
      parseDescriptionAuthors from $url:
        $authors
        $editor
        $editorDeputy
        $description
      """)

      (description, authors.getOrElse("").split(", ").toList, editor, editorDeputy.split(", ").toList)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject $defaultFrance2URL + $url " + e.toString)
        ("", Nil, "", Nil)
      }
    }
  }
}
