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
  val browser = Getter.getBrowser()

  val FRANCE2 = "France 2"
  val FRANCE3 = "France 3"
  implicit val ec = FutureService.ec

  def is13hTVShow(url: String) = {
    url.contains("13-heures")
  }

  def parseFranceTelevisionHomeHelper(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr"): List[News] = {
    val doc = browser.get(url)
    val nextNews = doc >> elementList(".list-jt-last a") >> attr("href")
    val headNews = doc >> element(".title a") >> attr("href")
    val allTelevisionNews = headNews :: nextNews
    val media = getMediaFranceTelevision(url)
    val (editor, editorDeputy) = if (media == FRANCE2) {
      parseTeam(is13hTVShow(url))
    } else {
      ("", List(""))
    }

    logger.info(s"""
      I got ${allTelevisionNews.length} days of news
    """)

    val parsedTelevisionNews = allTelevisionNews
      .map(televisionNewsForOneDay => {
        logger.info(s"Parsing this day of news : $televisionNewsForOneDay")
        parseFranceTelevisionNews(
          televisionNewsForOneDay,
          defaultUrl,
          media,
          editor,
          editorDeputy)
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
      media: String,
      editor: String,
      editorDeputy: List[String]): Future[List[Option[News]]] = {
    Future {
      try {
        val tvNewsURL = defaultUrl + url
        logger.debug("Parsing France TV news (presenter, editor, news) : " + tvNewsURL)

        val doc = browser.get(tvNewsURL)
        val news = doc >> elementList(".subjects-list li")
        val publishedDate = doc >> text(".schedule span:nth-of-type(1)") // Diffusé le 08/01/2022
        val presenter = doc >> text(".presenter .by")

        logger.debug(s"""
            This is what i got for this day $tvNewsURL:
            number of news: ${news.length}
            date : $publishedDate (${DateService.getTimestampFranceTelevision(publishedDate)})
            presenter : $presenter
            editor : $editor
          """)

        val parsedNews: List[Option[News]] = if (news.isEmpty) {
          List(None)
        } else {
          news.map(x => {
            val title = x >> text(".title")
            val order = x >> text(".number")
            val linkToDescription = x >> element(".title") >> attr("href")
            val (description, authors) = parseDescriptionAuthors(linkToDescription)

            logger.debug(s"""
              I got a news in order $order :
              title: $title
              editor: $editor
              editorDeputy: $editorDeputy
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
   * a AJAX query is sent to a URL to get week team on the website
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
  def parseTeam(
      noonNews: Boolean,
      default13hTeamURL: String =
        "https://www.francetvinfo.fr/esi/www/taxonomy/block-program-team-by-type-and-channel/channel/france-2/type/jt/taxonomyUrl/13-heures")
    : (String, List[String]) = {
    val url = if (noonNews) {
      default13hTeamURL
    } else {
      "https://www.francetvinfo.fr/esi/www/taxonomy/block-program-team-by-type-and-channel/channel/france-2/type/jt/taxonomyUrl/20-heures"
    }
    val doc = browser.get(url)
    val weekTeam = doc >> elementList(".team:nth-of-type(1) li")

    val (editor, editorDeputy) = weekTeam.isEmpty match {
      case false =>
        logger.debug(s"parseTeam : ${(weekTeam.head >?> text("li"))}")
        val editor = (weekTeam.head >?> text("li"))
          .getOrElse("Rédaction en chef")
          .replaceFirst("Rédaction en chef", "")
        val editorDeputy =
          (weekTeam.tail.head >?> text("li"))
            .getOrElse("Rédaction en chef-adjointe")
            .replaceFirst("Rédaction en chef-adjointe", "")
            .replaceFirst(" et ", ", ")
        logger.debug(s"weekTeam $editor, ${editorDeputy} ")

        (editor, editorDeputy)
      case true =>
        logger.info(s"No editor found ${weekTeam}")
        ("", "")
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

      logger.debug(s"""
      parseDescriptionAuthors from $url:
        authors: $authors
        subtitle: $subtitle
        description: $description
      """)

      (subtitle + description, authors.getOrElse("").split(", ").toList)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject $defaultFrance2URL + $url " + e.toString)
        ("", Nil)
      }
    }
  }
}
