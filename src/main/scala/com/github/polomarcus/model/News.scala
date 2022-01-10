package com.github.polomarcus.model

import java.util.Date


case class News (title: String,
                 description: String,
                 date: Date,
                 order: Integer,
                 presenter: String,
                 authors: List[String],
                 redactor: String,
                // redactorDeputy: List[String],
                // publisher: String,
                 url: String)