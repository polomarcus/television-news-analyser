package com.github.polomarcus.utils

import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.text.SimpleDateFormat
import java.util.Date
import com.typesafe.scalalogging.Logger

object DateService {
  val logger = Logger(DateService.getClass)

  def getTimestampFrance2(date: String) = {
    try {
      val format = new SimpleDateFormat("dd/MM/yyyy")
      format.parse(date.substring(11))
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this date $date " + e.toString)
        new Date()
      }
    }
  }
}
