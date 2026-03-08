import com.github.polomarcus.html.ParserTF1
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

class ParserTF1Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseTF1HomeHelper") {
    val listNews = ParserTF1.parseTF1HomeHelper(s"$localhost/home-tv-news-tf1.html", localhost)

    val description =
      "▶\uFE0F Il y a 70 millions de catholiques aux États-Unis. L'élection d'un pape nord-américain est une première et pourrait marquer un tournant, notamment face à la crise migratoire entre le Mexique et les États-Unis. Lors d\u2019une messe émouvante à la  frontière, le pape François avait pleuré face à cette situation. Cette nomination est perçue comme un signe d'espoir et de bénédiction. Le choix du nouveau pape de s'appeler Léon XIV s'inspire de Léon XIII, connu pour son engagement envers les pauvres. (International)."
      val news = News(
      "Léon XIV : un pape face aux défis des migrants",
      description,
      DateService.getTimestampTF1("Publié le 8 mai 2025 à 20h08"),
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
      "▶\uFE0F Il y a 70 millions de catholiques aux États-Unis. L'élection d'un pape nord-américain e" == description.take(90))
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
