package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService.waitFuture

import java.util.concurrent.Executors
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import com.github.polomarcus.utils.{DateService, FutureService, TextService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.Future

// For implicit conversions from RDDs to DataFrames
import scala.concurrent.ExecutionContext

object ParserFranceTelevision {
  val logger = Logger(this.getClass)
  val browser = JsoupBrowser()
  val FRANCE2 = "France 2"
  val FRANCE3 = "France 3"
  implicit val ec = FutureService.ec

  def parseFranceTelevisionHomeHelper(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr"): List[News] = {
    val doc = browser.get(url)
    val allTelevisionNews = doc >> elementList("a.flowItem") >> attr("href")

    val media = getMediaFranceTelevision(url)

    logger.info(s"""
      I got ${allTelevisionNews.length} days of news
    """)

    val parsedTelevisionNews = allTelevisionNews.map(televisionNewsForOneDay => {
      logger.info(s"Parsing this day of news : $televisionNewsForOneDay")
      parseFranceTelevisionNews(televisionNewsForOneDay, defaultUrl, media)
    })

    waitFuture[Option[News]](parsedTelevisionNews).flatten
  }
  def parseFranceTelevisionHome(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr") = {
    logger.debug("France television Url: " + url)

    try {
      parseFranceTelevisionHomeHelper(url, defaultUrl)
    } catch {
      case e: Exception =>
        logger.info(e.toString)
        try {
          //Try a second time in case of timeout
          Thread.sleep(2000L)
          parseFranceTelevisionHomeHelper(url, defaultUrl)
        } catch {
          case e: Exception => {
            logger.warn(s"Second exception in a row for, giving up $url " + e.toString)
            Nil
          }
        }
    }
  }

  def getMediaFranceTelevision(url: String): String = {
    url.contains("france-2") match {
      case true => FRANCE2
      case false => FRANCE3
    }
  }

  def parseFranceTelevisionNews(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr",
      media: String): Future[List[Option[News]]] = {
    Future {
      try {
        val tvNewsURL = defaultUrl + url
        logger.debug("France television Url: " + tvNewsURL)

        val doc = browser.get(tvNewsURL)
        val news = doc >> elementList(".subjects-list li")
        val publishedDate = doc >> text(".schedule span:nth-of-type(1)") // Diffusé le 08/01/2022
        val presenter = doc >> text(".presenter .by")

        logger.info(s"""
            This is what i got for this day $tvNewsURL:
            number of news: ${news.length}
            date : $publishedDate
            getTimestamp : ${DateService.getTimestampFranceTelevision(publishedDate)}
          """)

        val parsedNews: List[Option[News]] = if (news.isEmpty) {
          List(None)
        } else {
          news.map(x => {
            val title = x >> text(".title")
            val order = x >> text(".number")
            val linkToDescription = x >> element(".title") >> attr("href")
            val (description, authors, editor, editorDeputy) =
              parseDescriptionAuthors(linkToDescription)

            logger.debug(s"""
              I got a news in order $order :
              title: $title
              link to description : $linkToDescription
              description (30 first char): ${description.take(30)}
            """)

            Some(
              News(
                title,
                description,
                DateService.getTimestampFranceTelevision(publishedDate),
                order.toInt,
                presenter,
                authors,
                editor,
                editorDeputy,
                defaultUrl + linkToDescription,
                tvNewsURL,
                TextService.containsWordGlobalWarming(title + description),
                media))
          })
        }

        parsedNews
      } catch {
        case e: Exception => {
          logger.error(s"Error parsing this date $defaultUrl $url " + e.toString)
          Nil
        }
      }
    }
  }

  /**
   * Week team only
   * L'équipe de la semaine
    Rédaction en chef
    Elsa Pallot
    Rédaction en chef-adjointe
    Sébastien Renout, Anne Poncinet, Arnaud Comte

    @To do Weekend team one day..
   * @param doc
   * @return
   */
  def parseTeam(doc: browser.DocumentType): (String, List[String]) = {
    val weekTeam = doc >> elementList(".from-same-show__info-team:nth-of-type(1) li")

    val (editor, editorDeputy) = weekTeam.isEmpty match {
      case false =>
        val editor = (weekTeam.head >?> text("p:nth-of-type(2)")).getOrElse("")
        val editorDeputy = (weekTeam.tail.head >?> text("p:nth-of-type(2)")).getOrElse("")
        (editor, editorDeputy)
      case true => ("", "")
    }

    (editor, editorDeputy.split(", ").toList)
  }

  def parseSubtitle(doc: browser.DocumentType): String = {
    (doc >?> text(".c-chapo")).getOrElse("")
  }

  def parseDescriptionAuthors(
      url: String,
      defaultFrance2URL: String = "https://www.francetvinfo.fr") = {
    try {
      val doc: browser.DocumentType = browser.get(defaultFrance2URL + url)
      val descriptionOption = doc >?> text(".c-body")
      val subtitle = parseSubtitle(doc)
      val description = descriptionOption match {
        case Some(descriptionValue) => descriptionValue
        case None => {
          val oldDescription = (doc >?> text("#col-middle")).getOrElse("")
          oldDescription.split("Le JT")(0) // hack to proper extract description from old pages @see https://www.francetvinfo.fr/attaque-chimique-en-syrie-quelles-consequences_397173.html
        } // old tv news
      }
      val authors = doc >?> text(".c-signature__names span")

      val (editor, editorDeputy) = parseTeam(doc)

      logger.info(s"""
      parseDescriptionAuthors from $url:
        $authors
        $editor
        $editorDeputy
        $subtitle
        $description
      """)

      (subtitle + description, authors.getOrElse("").split(", ").toList, editor, editorDeputy)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject $defaultFrance2URL + $url " + e.toString)
        ("", Nil, "", Nil)
      }
    }
  }
}
