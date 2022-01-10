package com.github.polomarcus.html

import com.github.polomarcus.model.News
import com.github.polomarcus.utils.{DateService, FutureService}
import com.typesafe.scalalogging.Logger
import net.ruippeixotog.scalascraper.browser.{JsoupBrowser}

import scala.concurrent.Future

//https://github.com/ruippeixotog/scala-scraper
import java.net.URLEncoder

object Getter {
  val logger = Logger(this.getClass)
  implicit val ec = FutureService.ec
  val browser = JsoupBrowser()
  val france2UrlPagination = "https://www.francetvinfo.fr/replay-jt/france-2/20-heures"

  /**
   * https://www.francetvinfo.fr/replay-jt/france-2/20-heures/${args(0)}.html
   * @param Pagination start 1
   * @param Pagination end until 173+
   */
  def getFrance2News(start: Integer, end: Integer) = {
    val futureList = (start.toLong to end.toLong by 1).map { page =>
      try {
        Parser.parseFrance2News(s"$france2UrlPagination/$page.html")
      } catch {
        case e: Exception =>
          logger.info(e.toString)
          try {
            //Try a second time in case of timeout
            Parser.parseFrance2News(s"$france2UrlPagination/$page.html")
          } catch {
            case e: Exception => {
              logger.info("Second exception in a row " + e.toString)
              Future {
                Nil
              }
            }
          }
      }
    }

    FutureService.waitFuture[News](futureList)
  }
}
