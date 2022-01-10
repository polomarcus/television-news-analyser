package com.github.polomarcus.utils

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime, ZoneId}
import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.scalalogging.Logger

object DateService {
  val logger = Logger(DateService.getClass)


  def getTimestamp(date: String) = {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz")
    format.parse(date)
  }
}
