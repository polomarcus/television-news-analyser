package com.github.polomarcus.utils

import com.typesafe.scalalogging.Logger

object TextService {
    val logger = Logger(TextService.getClass)

    def containsWordGlobalWarming(description: String) : Boolean = {
      description.toLowerCase().contains("réchauffement climatique") || description.toLowerCase().contains("changement climatique")
    }
}
