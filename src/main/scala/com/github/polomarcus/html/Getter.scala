package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.{FutureService}
import com.typesafe.scalalogging.Logger

object Getter {
  val logger = Logger(this.getClass)
  implicit val ec = FutureService.ec

  val france2UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-2/20-heures"
  val france3UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-3/19-20/"
  val tf1UrlPagination = "https://www.tf1info.fr/emission/le-20h-11001/extraits"

  /**
   * 2 choices to parse :
   * * https://www.francetvinfo.fr/replay-jt/france-2/20-heures/${args(0)}.html
   * * https://www.tf1info.fr/emission/le-20h-11001/extraits/${args(0)}
   * @param Pagination start 1
   * @param Pagination end until 156
   * @param media: tf1 or france3 or france2 (default)
   */
  def getNews(start: Integer, end: Integer, media: String): List[News] = {
    val urlMedia = media match {
      case "tf1" => tf1UrlPagination
      case "france3" => france3UrlPagination
      case _ => france2UrlPagination
    }
    val newsList = (start.toLong to end.toLong by 1).map { page =>
      val url = if (page == 1) {
        s"$urlMedia"
      } else {
        s"$urlMedia/$page.html"
      }

      logger.info(s"Parsing this $url")
      media match {
        case "tf1" => ParserTF1.parseTF1Home(url)
        case _ => ParserFranceTelevision.parseFranceTelevisionHome(url)
      }
    }

    newsList.flatten.toList
  }
}
