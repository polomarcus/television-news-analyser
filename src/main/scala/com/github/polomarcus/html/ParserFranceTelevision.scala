package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService.waitFuture
import com.github.polomarcus.utils.{DateService, FutureService, TextService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

import java.sql.Timestamp
import scala.concurrent.Future

// For implicit conversions from RDDs to DataFrames

object ParserFranceTelevision {
  val logger = Logger(this.getClass)
  val browser = Getter.getBrowser()

  val htmlSelectorDayOfNewsList = ".page-jt article a"
  val htmlSelectorAllNewsFromOneDay = ".related-video-excerpts li"
  val htmlSelectorANewsFromOneDay = ".related-video-excerpts__link"
  val htmlSelectorMainDescriptionOfTheNews = ".c-chapo"
  val htmlSelectorTimeNews = ".publication-date__published time"

  val FRANCE2 = "France 2"
  val FRANCE3 = "France 3"
  implicit val ec = FutureService.ec

  def is13hTVShow(url: String) = {
    url.contains("13-heures")
  }

  def parseFranceTelevisionHomeHelper(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr"): List[News] = {
    val numberOfDaysToParse = 5
    val doc = browser.get(url)
    val allTelevisionNews = doc >> elementList(htmlSelectorDayOfNewsList) >> attr("href")
    val media = getMediaFranceTelevision(url)
    val arrayEditorAndDeputiesWeekorWeekend = if (media == FRANCE2) {
      Some(parseTeam(is13hTVShow(url)))
    } else {
      None
    }

    logger.info(s"""
        I got ${allTelevisionNews.length} days of news
    """)

    val parsedTelevisionNews = allTelevisionNews
      .take(numberOfDaysToParse)
      .map(televisionNewsForOneDay => {
        logger.info(s"Parsing this day of news : $televisionNewsForOneDay")
        parseFranceTelevisionNews(
          televisionNewsForOneDay,
          defaultUrl,
          media,
          arrayEditorAndDeputiesWeekorWeekend)
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
        logger.error(s"Could not parse $url, error : ${e.toString}")
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

  def getPresenter(text: String) = {
    text
      .split("présenté par ")(1)
      .split(" sur France")(0)
  }

  def getLinkToDescription(x: Element): String = {
    val linkToDescription = x >?> element(htmlSelectorANewsFromOneDay) >> attr("href")
    logger.debug(s"linkToDescription: $linkToDescription")
    linkToDescription match {
      case Some(link) => link
      case None =>
        logger.error("Could not read link to description")
        ""
    }
  }

  def getTitle(x: Element): String = {
    val titleOption = x >?> text(".related-video-excerpts__title-program")
    logger.debug(s"title: $titleOption")

    titleOption match {
      case Some(title) => title
      case None =>
        logger.error("Could not read title")
        ""
    }
  }

  /**
   * @TODO only work for France 2
   * @param editorAndDeputies
   * @param newsTimestamp
   * @return
   */
  def getEditor(editorAndDeputiesOption: Option[Array[(String, List[String])]], newsTimestamp: Timestamp) : (String, List[String]) = {
    editorAndDeputiesOption match {
      case Some(editorAndDeputies) => {
        if (DateService.isItaWeekendOrFridayNight(newsTimestamp)) {
          editorAndDeputies(1) //friday night and weekend
        } else {
          editorAndDeputies(0) // week
        }
      }
      case None => ("", List(""))
    }
  }

  def parseFranceTelevisionNews(
      url: String,
      defaultUrl: String = "https://www.francetvinfo.fr",
      media: String,
      editorAndDeputies: Option[Array[(String, List[String])]] ): Future[List[Option[News]]] = {
    Future {
      try {
        val tvNewsURL = defaultUrl + url
        logger.debug(s"Parsing France TV news (presenter, editor, news) : " + tvNewsURL)

        val doc = browser.get(tvNewsURL)
        val news = doc >> elementList(htmlSelectorAllNewsFromOneDay)

        val presenter = getPresenter(doc >> text(htmlSelectorMainDescriptionOfTheNews))

        logger.debug(s"""
            This is what i got for this day $tvNewsURL:
            number of news: ${news.length}
            presenter : $presenter
            editor : $editorAndDeputies
          """)

        val parsedNews: List[Option[News]] = if (news.isEmpty) {
          List(None)
        } else {
          news.zipWithIndex.map {
            case (x, index) => {
              val title = getTitle(x)

              val order = index + 1 // Since oct 2022, frtv has removed the order attribute

              val linkToDescription = getLinkToDescription(x)

              parseDescriptionAuthors(linkToDescription, defaultUrl) match {
                case Some((description, authors, publishedDate)) => {
                  val newsTimestamp: Timestamp = DateService.getTimestampFranceTelevision(publishedDate)

                  val (editor, editorDeputy) = getEditor(editorAndDeputies, newsTimestamp)

                  logger.debug(s"""
                  I got a news in order $order :
                  title: $title
                  publishedDate: $publishedDate
                  editor: $editor
                  editorDeputy: $editorDeputy
                  link to description : $linkToDescription
                  description (30 first char): ${description.take(30)}
                """)

                  Some(
                    News(
                      title,
                      description,
                      newsTimestamp,
                      order.toInt,
                      presenter,
                      authors,
                      editor,
                      editorDeputy,
                      linkToDescription,
                      tvNewsURL,
                      TextService.containsWordGlobalWarming(title + description),
                      media))
                }
                case None =>
                  logger.error(s"Could not parse this news $linkToDescription")
                  None
              }
            }
          }
        }

        parsedNews
      } catch {
        case e: Exception => {
          logger.error(s"Error parsing this date $url " + e.toString)
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
  
   * @param doc
   * @return 1st element week team, 2nd element weekend team
   */
  def parseTeam(
      noonNews: Boolean,
      default13hTeamURL: String =
        "https://www.francetvinfo.fr/esi/www/taxonomy/block-program-team-by-type-and-channel/channel/france-2/type/jt/taxonomyUrl/13-heures")
    : Array[(String, List[String])] = {
    val url = if (noonNews) {
      default13hTeamURL
    } else {
      "https://www.francetvinfo.fr/esi/www/taxonomy/block-program-team-by-type-and-channel/channel/france-2/type/jt/taxonomyUrl/20-heures"
    }
    val doc = browser.get(url)

    val weekTeam: Seq[Element] = doc >> elementList(s".team:nth-of-type(1) li")
    val weekEndTeam = doc >> elementList(s".team:nth-of-type(2) li")

    Array(
      parseTeamHelper(weekTeam),
      parseTeamHelper(weekEndTeam)
    )
  }

  def parseTeamHelper(team: Seq[Element] ) : (String, List[String]) = {
    team.isEmpty match {
      case false =>
        logger.debug(s"parseTeam : ${(team.head >?> text("li"))}")
        val editor = (team.head >?> text("li"))
          .getOrElse("Rédaction en chef")
          .replaceFirst("Rédaction en chef", "")
        val editorDeputy =
          (team.tail.head >?> text("li"))
            .getOrElse("Rédaction en chef-adjointe")
            .replaceFirst("Rédaction en chef-adjointe", "")
            .replaceFirst(" et ", ", ")
        logger.debug(s"weekTeam $editor, ${editorDeputy} ")

        (editor, editorDeputy.split(", ").toList)
      case true =>
        logger.info(s"No editor found ${team}")
        ("",List(""))
    }
  }

  def parseSubtitle(doc: browser.DocumentType): String = {
    (doc >?> text(htmlSelectorMainDescriptionOfTheNews)).getOrElse("")
  }

  def getDate(doc: browser.DocumentType) = {
    val dateOption = doc >?> text(htmlSelectorTimeNews) // "le 08/10/2022 22:26"
    logger.debug(s"dateOption: $dateOption")

    dateOption match {
      case Some(date) => date
      case None =>
        logger.error("Could not read date")
        ""
    }

  }
  def parseDescriptionAuthors(
      url: String,
      defaultFrance2URL: String = "https://www.francetvinfo.fr")
    : Option[(String, List[String], String)] = {
    try {
      val newsUrl = if (url.contains(defaultFrance2URL)) {
        url
      } else {
        defaultFrance2URL + url
      }
      logger.debug(s"parseDescriptionAuthors from $newsUrl")
      val doc: browser.DocumentType = browser.get(newsUrl)
      val publishedDate = getDate(doc)

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
        authors: $authors
        subtitle: $subtitle
        description: $description
        publishedDate: $publishedDate
      """)

      Some((subtitle + description, authors.getOrElse("").split(", ").toList, publishedDate))
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject : $url " + e.toString)
        None
      }
    }
  }
}
