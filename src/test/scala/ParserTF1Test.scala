import com.github.polomarcus.html.ParserTF1
import com.github.polomarcus.model.News
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.util.Date

class ParserTF1Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseTF1HomeHelper") {
    val listNews = ParserTF1.parseTF1HomeHelper(s"$localhost/home-tv-news-tf1.html", localhost)

    val description = ""
    val news = News("JT20H - Jardinier, cadre, formateur … dans cette entreprise, les salariés sont tous handicapés",
      description,
      new Timestamp(new Date("11/16/2016").getTime),
      0,
      "",
      Nil,
      "",
      Nil,
      "http://localhost:8000/one-subject-tv-news-tf1.html",
      "http://localhost:8000/home-tv-news-tf1.html",
      containsWordGlobalWarming = false,
      ParserTF1.TF1)

    assert(news == listNews.head)
    assert(listNews.length == 7)
  }

  test("parseDescriptionAuthors") {
    val (description, authors, editor, editorDeputy) = ParserTF1.parseDescriptionAuthors("/one-subject-tv-news-tf1.html", localhost)

    assert("L'essentiel Certes, ils sont en général moins chers, mais ils ont la réputation d’être de " == description.take(90))
    assert(List("La rédaction") == authors)
    assert("" == editor)
    assert(List("") == editorDeputy)
  }
}
