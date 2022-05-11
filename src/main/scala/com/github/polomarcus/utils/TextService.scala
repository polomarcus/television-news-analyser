package com.github.polomarcus.utils

import com.typesafe.scalalogging.Logger

import scala.util.matching.Regex

object TextService {
  val logger = Logger(TextService.getClass)
  val pattern = new Regex(
    "(réchauffement|dérèglement|changement|crise|enjeux|volet)(|s) climatique(|s)")

  //"Climat :" comes from  https://www.francetvinfo.fr/meteo/secheresse/climat-pourquoi-la-france-connait-elle-une-secheresse-precoce_5125996.html
  val pattern2 = new Regex("((réchauffement|dérèglement|changement) du climat)|(giec)|(climat :)")
  def containsWordGlobalWarming(description: String): Boolean = {
    pattern.findFirstIn(description.toLowerCase).isDefined || pattern2
      .findFirstIn(description.toLowerCase)
      .isDefined
  }
}
