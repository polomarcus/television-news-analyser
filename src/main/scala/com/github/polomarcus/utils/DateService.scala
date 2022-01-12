package com.github.polomarcus.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import com.typesafe.scalalogging.Logger

object DateService {
  val logger = Logger(DateService.getClass)

  def getTimestampFrance2(date: String): Timestamp = {
    try {
      val format = new SimpleDateFormat("dd/MM/yyyy")
      new Timestamp(format.parse(date.substring(11)).getTime)
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this date $date " + e.toString)
        new Timestamp(System.currentTimeMillis())
      }
    }
  }
}
