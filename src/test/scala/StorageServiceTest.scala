import com.github.polomarcus.html.{ParserFranceTelevision, ParserTF1}
import com.github.polomarcus.model.News
import com.github.polomarcus.storage.StorageService
import com.github.polomarcus.utils.SparkService
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.util.Date

class StorageServiceTest extends AnyFunSuite {
  val spark = SparkService.getAndConfigureSparkSession()
  val sqlContext = spark.sqlContext

  val description =
    "L'essentiel Certes, ils sont en général moins chers, mais ils ont la réputation d’être de moins bonne qualité que les articles de grandes marques. Un produit sur trois, vendus en France, est une marque distributeur. Que valent-ils vraiment ?  ans les rayons, elles sont partout. U, Carrefour, Auchan, Franprix, ce sont les marques des distributeurs. Leur vente ne cesse d’augmenter. Ces produits coûtent en moyenne 30% de moins que les marques nationales. Des petits prix qui séduisent les consommateurs. Produits d’hygiène, pâtes, surgelés, pour fidéliser leurs clients, les distributeurs créent sans cesse de nouveaux produits.  Un produit sur trois vendus en France est une marque de distributeur. Que valent-ils vraiment ? Sont-ils moins bons ou meilleurs que les grandes marques ? Première surprise, ils sont souvent fabriqués dans les mêmes usines. Comme ici, des salades sont mises en sachet pour une marque de distributeur. À droite, pour une marque nationale.  Mais il y a quand même quelques différences. En clair, les grandes marques privilégient les parties les plus nobles. C’est ce qui explique l'écart de prix. Mais ce n’est pas la seule raison. Avec les marques nationales, vous payez bien plus que le produit. Et en regardant de près les étiquettes, on apprend que les qualités nutritionnelles sont souvent identiques.  T F1 | Reportage L. Deschateaux, M. Derre, V. Daran"
  val news = News(
    "Léon XIV : un pape face aux défis des migrants",
    description,
    new Timestamp(new Date("11/16/2016").getTime),
    0,
    "",
    List("L. Deschateaux", "M. Derre", "V. Daran"),
    "",
    List(""),
    "http://localhost:8000/one-subject-tv-news-tf1.html",
    "http://localhost:8000/home-tv-news-tf1.html",
    containsWordGlobalWarming = false,
    ParserTF1.TF1)

  val newsFr2 = News(
    "Brésil : effondrement meurtrier d'une falaise sur un groupe de touristes changement climatique",
    description,
    new Timestamp(new Date("01/09/2022").getTime),
    1,
    "Laurent Delahousse",
    List("C.Verove", "O.Sauvayre"),
    "Elsa Pallot",
    List("Thibaud de Barbeyrac"),
    "http://localhost:8000/monde/bresil/bresil-effondrement-meurtrier-d-une-falaise-sur-un-groupe-de-touristes_4910403.html",
    "http://localhost:8000/replay.html",
    containsWordGlobalWarming = true,
    ParserFranceTelevision.FRANCE2)

  val newsFr2Second = newsFr2.copy(containsWordGlobalWarming = false)
  val newsTF1Second = news.copy(containsWordGlobalWarming = true)
  val newsTF1Third = news.copy(containsWordGlobalWarming = true)

  test("write") {
    val listNews = List(news, newsFr2, newsFr2Second, newsTF1Second, newsTF1Third)
    assert(StorageService.write(listNews, "test-test") == "test-test")
  }
}
