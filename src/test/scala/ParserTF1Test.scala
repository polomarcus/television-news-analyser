import com.github.polomarcus.html.ParserTF1
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

class ParserTF1Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseTF1HomeHelper") {
    val listNews = ParserTF1.parseTF1HomeHelper(s"$localhost/home-tv-news-tf1.html", localhost)

    val description =
      "En 2023, vous avez peut-être fait partie des 27 millions de Français à avoir tenté votre chance à un jeu de hasard. Comment ces tickets sont-ils fabriqués ? Qui décide que telle ou telle carte imprimée sera gagnante ?"
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
      "En 2023, vous avez peut-être fait partie des 27 millions de Français à avoir tenté votre c" == description
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
