package com.github.polomarcus.utils

import java.sql.Timestamp
import java.text.SimpleDateFormat
import com.typesafe.scalalogging.Logger
import java.util.Calendar

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

  def parseFrenchMonth(month: String) = {
    month match {
      case "janvier" => "01"
      case "février" => "02"
      case "mars" => "03"
      case "avril" => "04"
      case "mai" => "05"
      case "juin" => "06"
      case "juillet" => "07"
      case "août" => "08"
      case "septembre" => "09"
      case "octobre" => "10"
      case "novembre" => "11"
      case "décembre" => "12"
      case _  =>
        logger.error("Error parsign month", month)
        "Invalid month"  // the default, catch-all
    }
  }
  //"Publié le 10 décembre 2020 à 20h08"
  // Publié hier à 20h39
  def getTimestampTF1(date: String): Timestamp = {
    try {
      val format = new SimpleDateFormat("d/MM/yyyy")
      // Create a calendar object with today date. Calendar is in java.util pakage.
      val calendar = Calendar.getInstance
      if(date.contains("hier")) { // late publish
        // Move calendar to yesterday
        calendar.add(Calendar.DATE, -1)

        // Get current date of calendar which point to the yesterday now
        new Timestamp(calendar.getTime.getTime)
      } else if ( date.contains("aujourd’hui") ) {
        new Timestamp(calendar.getTime.getTime)
      } else {
        val dateSplit = date.split(" ")
        val day = dateSplit(2)
        val month = parseFrenchMonth(dateSplit(3))
        val year = dateSplit(4)
        new Timestamp(format.parse(s"$day/$month/$year").getTime)
      }
    } catch {
      case e: Exception => {
        logger.error(s"Error parsing this date $date using today date" + e.toString)
        new Timestamp(System.currentTimeMillis())
      }
    }
  }
}
