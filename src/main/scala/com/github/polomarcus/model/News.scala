package com.github.polomarcus.model

import java.util.Date


case class News (title: String,
                 description: String,
                 date: Date,
                 order: Integer,
                 presenter: String,
                 authors: List[String],
                 editor: String,
                 editorDeputy: List[String],
                 url: String,
                 urlTvNews: String,
                 containsClimate: Boolean)