package com.github.polomarcus.model

import java.sql.Timestamp


case class News (title: String,
                 description: String,
                 date: Timestamp,
                 order: Long,
                 presenter: String,
                 authors: List[String],
                 editor: String,
                 editorDeputy: List[String],
                 url: String,
                 urlTvNews: String,
                 containsWordGlobalWarming: Boolean,
                 media: String)