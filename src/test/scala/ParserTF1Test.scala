import com.github.polomarcus.html.Getter.logger
import com.github.polomarcus.html.ParserTF1
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.util.Date

class ParserTF1Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseTF1HomeHelper") {
    val listNews = ParserTF1.parseTF1HomeHelper(s"$localhost/home-tv-news-tf1.html", localhost)

    val description =
      "L'essentiel Certes, ils sont en général moins chers, mais ils ont la réputation d’être de moins bonne qualité que les articles de grandes marques. Un produit sur trois, vendus en France, est une marque distributeur. Que valent-ils vraiment ?  ans les rayons, elles sont partout. U, Carrefour, Auchan, Franprix, ce sont les marques des distributeurs. Leur vente ne cesse d’augmenter. Ces produits coûtent en moyenne 30% de moins que les marques nationales. Des petits prix qui séduisent les consommateurs. Produits d’hygiène, pâtes, surgelés, pour fidéliser leurs clients, les distributeurs créent sans cesse de nouveaux produits.  Un produit sur trois vendus en France est une marque de distributeur. Que valent-ils vraiment ? Sont-ils moins bons ou meilleurs que les grandes marques ? Première surprise, ils sont souvent fabriqués dans les mêmes usines. Comme ici, des salades sont mises en sachet pour une marque de distributeur. À droite, pour une marque nationale.  Mais il y a quand même quelques différences. En clair, les grandes marques privilégient les parties les plus nobles. C’est ce qui explique l'écart de prix. Mais ce n’est pas la seule raison. Avec les marques nationales, vous payez bien plus que le produit. Et en regardant de près les étiquettes, on apprend que les qualités nutritionnelles sont souvent identiques.  T F1 | Reportage L. Deschateaux, M. Derre, V. Daran"
    val news = News(
      "JT20H - Jardinier, cadre, formateur … dans cette entreprise, les salariés sont tous handicapés",
      description,
      DateService.getTimestampTF1("Publié le 16 novembre 2016 à 20h38"),
      0,
      "",
      List("L. Deschateaux", "M. Derre", "V. Daran"),
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
      "L'essentiel Certes, ils sont en général moins chers, mais ils ont la réputation d’être de " == description
        .take(90))
    assert(List("L. Deschateaux", "M. Derre", "V. Daran") == authors)
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
