package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.{ FutureService}
import com.typesafe.scalalogging.Logger


import scala.concurrent.Future

//https://github.com/ruippeixotog/scala-scraper
import java.net.URLEncoder

object Getter {
  val logger = Logger(this.getClass)
  implicit val ec = FutureService.ec

  val france2UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-2/20-heures"

  /**
   * https://www.francetvinfo.fr/replay-jt/france-2/20-heures/${args(0)}.html
   * @param Pagination start 1
   * @param Pagination end until 173+
   */
  def getFrance2News(start: Integer, end: Integer) : List[News] = {
    val newsList = (start.toLong to end.toLong by 1).map { page =>
      val url = if(page == 1) {
        s"$france2UrlPagination" // https://www.francetvinfo.fr/replay-jt/france-2/20-heures/
      } else {
        s"$france2UrlPagination/$page.html" // https://www.francetvinfo.fr/replay-jt/france-2/20-heures/2.html
      }

      logger.info(url)
      Parser.parseFrance2Home(url)
    }

    newsList.flatten.toList
  }
}
