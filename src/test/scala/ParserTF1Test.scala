import com.github.polomarcus.html.ParserTF1
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

class ParserTF1Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseTF1HomeHelper") {
    val listNews = ParserTF1.parseTF1HomeHelper(s"$localhost/home-tv-news-tf1.html", localhost)

    val description =
      "▶\uFE0F Qualifiés, mais pas vraiment dominateurs, les Bleus disputeront dans trois jours le quart de finale de l'Euro. Après leur victoire lundi soir à 1-0 face à la Belgique. Comment les joueurs ont-ils réagi après le match ? (Euro 2024) -  TF1 INFO"
      val news = News(
      "JT20H - Jardinier, cadre, formateur … dans cette entreprise, les salariés sont tous handicapés",
      description,
      DateService.getTimestampTF1("Publié le 16 novembre 2016 à 20h38"),
      0,
      "",
      Nil,
      "",
      List(""),
      "http://localhost:8000/one-subject-tv-news-tf1.html",
      "http://localhost:8000/home-tv-news-tf1.html",
      containsWordGlobalWarming = false,
      ParserTF1.TF1)

    assert(news.title == listNews.head.title)
    assert(news.description == listNews.head.description)
    assert(news.date == listNews.head.date)
    assert(news.order == listNews.head.order)
    assert(news.presenter == listNews.head.presenter)
    assert(news.authors == listNews.head.authors)
    assert(news.editor == listNews.head.editor)
    assert(news.editorDeputy == listNews.head.editorDeputy)
    assert(news.url == listNews.head.url)
    assert(news.urlTvNews == listNews.head.urlTvNews)
    assert(news.containsWordGlobalWarming == listNews.head.containsWordGlobalWarming)
    assert(news.media == listNews.head.media)
    assert(listNews.length == 50)
  }

  test("parseDescriptionAuthors") {
    val (description, authors, editor, editorDeputy) =
      ParserTF1.parseDescriptionAuthors("/one-subject-tv-news-tf1.html", localhost)

    assert(
      "▶\uFE0F Qualifiés, mais pas vraiment dominateurs, les Bleus disputeront dans trois jours le qua" == description
        .take(90))
    assert(Nil == authors)
    assert("" == editor)
    assert(List("") == editorDeputy)
  }

  test("parseAuthors") {
    assert(
      ParserTF1.parseAuthors("T F1 | Reportage T. Jarrion, F. Couturon, F. Petit") == List(
        "T. Jarrion",
        "F. Couturon",
        "F. Petit"))
    assert(ParserTF1.parseAuthors("T F1 | Reportage T. Jarrion") == List("T. Jarrion"))
    assert(ParserTF1.parseAuthors("F. Petit") == List(""))
  }
}
