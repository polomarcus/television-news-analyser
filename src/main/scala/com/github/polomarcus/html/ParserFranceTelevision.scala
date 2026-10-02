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
  val htmlSelectorAllNewsFromOneDay = ".generic-vertical-rebound__item"
  val htmlSelectorANewsFromOneDay = ".card-article-list-s__link"
  val htmlSelectorMainDescriptionOfTheNews = ".c-chapo"
  val htmlSelectorTimeNews = ".publication-date__published time"
  // Since 2026, France TV loads the list of news for one day from a separate ESI
  // (Edge Side Include) endpoint, referenced via the data-esi-url attribute on the day page.
  val htmlSelectorSameShowEsi = "[data-esi-url*=SameShowESI]"
  // Since Oct 2026, the JT team (presenter, editor in chief and deputies) is rendered inline
  // on the replay page, in a "L'équipe du JT" section : one block for the week team,
  // one for the week-end team. The old ESI endpoint (@see parseTeam) now answers a 404.
  val htmlSelectorTeamContent = ".program-team__info-team-content"
  val htmlSelectorTeamItem = ".program-team__info-team-item"

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
      // the team is now part of the replay page itself, the ESI endpoint is only a fallback
      // for older pages
      parseTeamFromDoc(doc).orElse(parseTeamSafely(is13hTVShow(url)))
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
      val presenters = (doc >> elementList(htmlSelectorTeamContent)).flatMap(teamContent => {
        getTeamItems(teamContent).collectFirst {
          case (label, name) if label.contains("Présenté par") => name
        }
      }).toArray
      if (presenters.isEmpty) Array("", "") else presenters
    } catch {
      case e: Exception =>
        logger.error(s"Error parsing presenter from team section: ${e.toString}")
        Array("", "")
    }
  }

  /**
   * Returns the list of news items shown on a given day page.
   *
   * Since 2026, France TV no longer renders the news list inline on the day page.
   * The page contains a placeholder element with a `data-esi-url` attribute pointing
   * to an ESI endpoint (`/esi-block/contents::SameShowESI/index/{...}.html`) that
   * returns the actual list. We follow that URL when present, and fall back to the
   * day page itself to preserve compatibility with older HTML structures (and tests).
   */
  def getNewsListForDay(doc: browser.DocumentType, defaultUrl: String): List[Element] = {
    getNewsListForDayHelper(doc, defaultUrl).filterNot(news =>
      isLinkToAnotherTVShow(getLinkToDescription(news)))
  }

  /**
   * We scrap a JT shortly after it has been broadcast : its subjects are sometimes not published
   * yet, and the "Les sujets du JT" block then falls back to listing the other JT of the week.
   * Those are not news subjects, e.g. /replay-jt/france-2/20-heures/jt-de-20h-du-jeudi-01-octobre-2026_8187524.html
   */
  def isLinkToAnotherTVShow(link: String): Boolean = {
    link.contains("/jt-de-")
  }

  private def getNewsListForDayHelper(
      doc: browser.DocumentType,
      defaultUrl: String): List[Element] = {
    val inlineNews = doc >> elementList(htmlSelectorAllNewsFromOneDay)
    if (inlineNews.nonEmpty) {
      inlineNews
    } else {
      val esiUrlOpt = doc >?> element(htmlSelectorSameShowEsi) >> attr("data-esi-url")
      esiUrlOpt match {
        case Some(esiPath) if esiPath.nonEmpty =>
          val esiUrl = if (esiPath.startsWith("http")) esiPath else defaultUrl + esiPath
          // The ESI path embeds a JSON literal like /index/{"contentId":...}.html.
          // java.net.URL rejects the unescaped braces/quotes, so percent-encode them.
          val safeEsiUrl = esiUrl
            .replace("{", "%7B")
            .replace("}", "%7D")
            .replace("\"", "%22")
          logger.debug(s"Fetching news list from ESI block: $safeEsiUrl")
          val esiDoc = browser.get(safeEsiUrl)
          esiDoc >> elementList(htmlSelectorAllNewsFromOneDay)
        case _ =>
          logger.warn("No news list found on day page and no SameShowESI data-esi-url attribute")
          Nil
      }
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

  // The ESI block returns relative hrefs (e.g. /replay-jt/...): store them absolute
  // so the published data does not point to the wrong domain
  def toAbsoluteUrl(link: String, defaultUrl: String): String = {
    if (link.isEmpty || link.startsWith("http")) link else defaultUrl + link
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
        val news = getNewsListForDay(doc, defaultUrl)

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

              val linkToDescription = toAbsoluteUrl(getLinkToDescription(x), defaultUrl)

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
   * Reads the "L'équipe du JT" section rendered inline on a replay page (since Oct 2026).
   *
   * Each `.program-team__info-team-content` block is a team (1st : week, 2nd : week-end) made of
   * `<li><p>label</p><p>name(s)</p></li>` items, e.g. :
   *   Présenté par / Jean-Baptiste Marteau
   *   Rédacteurs en chef / Elsa Pallot
   *   Rédacteurs en chef adjoints / Julien Gasparutto, Margaux Manière
   *
   * @return None when the section is absent, so the caller can fall back to the ESI endpoint.
   *         Otherwise 1st element week team, 2nd element week-end team.
   */
  def parseTeamFromDoc(doc: browser.DocumentType): Option[Array[(String, List[String])]] = {
    try {
      val teams = (doc >> elementList(htmlSelectorTeamContent)).map(parseTeamContent).toArray

      teams.length match {
        case 0 => None
        // only one team published : use it for the week and the week-end
        case 1 => Some(Array(teams(0), teams(0)))
        case _ => Some(teams.take(2))
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error parsing the team section: ${e.toString}")
        None
    }
  }

  /**
   * @return label -> value of every item of a team block, e.g. ("Rédacteurs en chef", "Elsa Pallot")
   */
  def getTeamItems(teamContent: Element): List[(String, String)] = {
    (teamContent >> elementList(htmlSelectorTeamItem)).flatMap(item => {
      val paragraphs = item >> elementList("p")
      if (paragraphs.length > 1) {
        Some((paragraphs.head.text.trim, paragraphs(1).text.trim))
      } else {
        None
      }
    }).toList
  }

  def parseTeamContent(teamContent: Element): (String, List[String]) = {
    val items = getTeamItems(teamContent)
    // "Rédacteurs en chef" vs "Rédacteurs en chef adjoints" (the website used to write
    // "Rédaction en chef" and "Rédaction en chef-adjointe")
    val isEditorLabel = (label: String) => label.toLowerCase.contains("en chef")
    val isDeputyLabel = (label: String) => label.toLowerCase.contains("adjoint")

    val editor = items.collectFirst {
      case (label, names) if isEditorLabel(label) && !isDeputyLabel(label) => names
    }.getOrElse("")

    val editorDeputy = items.collectFirst {
      case (label, names) if isEditorLabel(label) && isDeputyLabel(label) =>
        names.replaceFirst(" et ", ", ").split(", ").toList
    }.getOrElse(List(""))

    logger.debug(s"parseTeamContent : $editor, $editorDeputy")
    (editor, editorDeputy)
  }

  /**
   * The ESI endpoint used by [[parseTeam]] has been answering a 404 since Oct 2026 : never let it
   * break the parsing of a whole day of news.
   */
  def parseTeamSafely(
      noonNews: Boolean,
      default13hTeamURL: String =
        "https://www.francetvinfo.fr/esi/www/taxonomy/block-program-team-by-type-and-channel/channel/france-2/type/jt/taxonomyUrl/13-heures")
    : Option[Array[(String, List[String])]] = {
    try {
      Some(parseTeam(noonNews, default13hTeamURL))
    } catch {
      case e: Exception =>
        logger.warn(s"Could not get the team from the ESI endpoint : ${e.toString}")
        None
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

      // Since June 2026, France TV article pages use .hero-video__title instead of .c-title
      val title = (doc >?> text(".c-title"))
        .orElse(doc >?> text(".hero-video__title"))
        .getOrElse("")
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
