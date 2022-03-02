package com.github.polomarcus.utils

import com.typesafe.scalalogging.Logger

import scala.util.matching.Regex

object TextService {
  val logger = Logger(TextService.getClass)
  val pattern = new Regex("(réchauffement|dérèglement|changement)(|s) climatique(|s)")
  val pattern2 = new Regex("(réchauffement|dérèglement|changement) du climat")
  def containsWordGlobalWarming(description: String): Boolean = {
    pattern.findFirstIn(description.toLowerCase).isDefined || pattern2
      .findFirstIn(description.toLowerCase)
      .isDefined
  }
}
