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
  val htmlSelectorANewsFromOneDay = ".card-article-list-s__link"
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
        I got ${allTelevisionNews.length} days of news, but parsing only ${numberOfDaysToParse} to avoid some wasteful requests
    """)

    val parsedTelevisionNews = allTelevisionNews
      .take(numberOfDaysToParse)
      .map(televisionNewsForOneDay => {
        logger.info(s"Day : $televisionNewsForOneDay")
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
    logger.info("France television Url: " + url)

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

  def getPresenter(text: String): String = {
    if (text.contains("présenté par ")) {
      text.split("présenté par ")(1).split(" sur France")(0)
    } else {
      ""
    }
  }

  /**
   * Extract presenters from the program-team section on the day page.
   * Returns an Array where index 0 = weekday presenter, index 1 = weekend presenter.
   * The HTML structure is:
   * <div class="program-team__info-team-content">
   *   <li class="program-team__info-team-item"><p>Présenté par</p><p>Name</p></li>
   *   ...
   * </div>
   */
  def getPresenterFromTeamSection(doc: browser.DocumentType): Array[String] = {
    try {
      val teamContents = doc >> elementList(".program-team__info-team-content")
      val presenters = teamContents.flatMap(teamContent => {
        val items = teamContent >> elementList(".program-team__info-team-item")
        items.flatMap(item => {
          val paragraphs = item >> elementList("p")
          if (paragraphs.length > 1 && paragraphs.head.text.trim.contains("Présenté par")) {
            Some(paragraphs(1).text.trim)
          } else {
            None
          }
        }).headOption
      }).toArray
      if (presenters.isEmpty) Array("", "") else presenters
    } catch {
      case e: Exception =>
        logger.error(s"Error parsing presenter from team section: ${e.toString}")
        Array("", "")
    }
  }

  def getLinkToDescription(x: Element): String = {
    val linkToDescription = x >?> element(htmlSelectorANewsFromOneDay) >> attr("href")
    logger.info(s"linkToDescription: $linkToDescription")
    linkToDescription match {
      case Some(link) => link
      case None =>
        logger.error("Could not read link to description")
        ""
    }
  }

  def getTitle(x: Element): String = {
    val titleOption = x >?> text(".card-article-list-s__title")
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

        val chapoText = (doc >?> text(htmlSelectorMainDescriptionOfTheNews)).getOrElse("")
        val presenterFromChapo = getPresenter(chapoText)
        val presenters: Array[String] = if (presenterFromChapo.nonEmpty) {
          Array(presenterFromChapo, presenterFromChapo)
        } else {
          getPresenterFromTeamSection(doc)
        }

        logger.info(s"""
            for $tvNewsURL:
            number of news: ${news.length}
            presenters : ${presenters.mkString(", ")}
          """)

        // Get the date from the day page itself
        val dayPageDate = getDate(doc)
        val newsTimestamp: Timestamp = DateService.getTimestampFranceTelevision(dayPageDate)

        val parsedNews: List[Option[News]] = if (news.isEmpty) {
          logger.info("No news to parse")
          List(None)
        } else {
          news.zipWithIndex.map {
            case (x, index) => {
              logger.debug("Parsing news :" + x)

              val order = index + 1 // Since oct 2022, frtv has removed the order attribute

              val linkToDescription = getLinkToDescription(x)

              parseDescriptionAuthors(linkToDescription, defaultUrl) match {
                case Some((title, description, authors, _)) => {

                  val (editor, editorDeputy) = getEditor(editorAndDeputies, newsTimestamp)

                  val presenter = if (presenters.length > 1 && DateService.isItaWeekendOrFridayNight(newsTimestamp)) {
                    presenters(1)
                  } else if (presenters.nonEmpty) {
                    presenters(0)
                  } else {
                    ""
                  }

                  logger.debug(s"""
                  I got a news in order $order :
                  title: $title
                  date: $dayPageDate
                  presenter: $presenter
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
    : Option[(String, String, List[String], String)] = {
    try {
      val newsUrl = if (url.contains(defaultFrance2URL) || url.contains("https://www.franceinfo.fr")) {
        url
      } else {
        defaultFrance2URL + url
      }
      logger.info(s"parseDescriptionAuthors from $newsUrl")
      val doc: browser.DocumentType = browser.get(newsUrl)
      val publishedDate = getDate(doc)

      val title = (doc >?> text(".c-title")).getOrElse("")
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

      logger.info(s"""
        title: $title
        authors: $authors
        subtitle: $subtitle
        description: $description
        publishedDate: $publishedDate
      """)

      Some((title, subtitle + description, authors.getOrElse("").split(", ").toList, publishedDate))
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this subject : $url " + e.toString)
        None
      }
    }
  }
}
