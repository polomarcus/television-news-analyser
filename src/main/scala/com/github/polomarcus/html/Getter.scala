package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.{FutureService}
import com.typesafe.scalalogging.Logger

object Getter {
  val logger = Logger(this.getClass)
  implicit val ec = FutureService.ec

  val france2UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-2/20-heures"
  val france213hUrlPagination = "https://www.francetvinfo.fr/replay-jt/france-2/13-heures"

  val france3UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-3/19-20"
  val france313hUrlPagination = "https://www.francetvinfo.fr/replay-jt/france-3/12-13"

  val tf1UrlPagination = "https://www.tf1info.fr/emission/le-20h-11001/extraits"
  val tf1WeekendUrlPagination = "https://www.tf1info.fr/emission/le-we-12559/extraits"
  val tf113hPagination = "https://www.tf1info.fr/emission/le-13h-10927/extraits"

  /**
   * 2 choices to parse :
   * * https://www.francetvinfo.fr/replay-jt/france-2/20-heures/${args(0)}.html
   * * https://www.tf1info.fr/emission/le-20h-11001/extraits/${args(0)}
   * @param Pagination start 1
   * @param Pagination end until 156
   * @param media: tf1 or france3 or france2 (default)
   */
  def getNews(start: Integer, end: Integer, media: String): List[News] = {
    val (urlMedia, urlMedia13h) = media match {
      case "tf1" => (tf1UrlPagination, tf113hPagination)
      case "france3" => (france3UrlPagination, france313hUrlPagination)
      case _ => (france2UrlPagination, france213hUrlPagination)
    }
    val newsList = (start.toLong to end.toLong by 1).map { page =>
      val pagination = getPagination(page)
      logger.info(s"Parsing this $urlMedia$pagination")

      media match {
        case "tf1" =>
          ParserTF1.parseTF1Home(s"$urlMedia$pagination") ++
            ParserTF1.parseTF1Home(s"$tf1WeekendUrlPagination$pagination") ++
            ParserTF1.parseTF1Home(s"$urlMedia13h$pagination")
        case _ =>
          ParserFranceTelevision.parseFranceTelevisionHome(s"$urlMedia$pagination") ++
            ParserFranceTelevision.parseFranceTelevisionHome(s"$urlMedia13h$pagination")
      }
    }

    newsList.flatten.toList
  }

  def getPagination(page: Long): String = {
    if (page == 1) {
      ""
    } else {
      s"/$page.html"
    }
  }
}
