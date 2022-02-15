import com.github.polomarcus.html
import com.github.polomarcus.html.Getter.logger
import com.github.polomarcus.html.ParserFR2
import com.github.polomarcus.model.News
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.time.LocalDate
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ParserFR2Test extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseFrance2Home") {
    val listNews = ParserFR2.parseFrance2Home(s"$localhost/home-tv-news-fr2.html", localhost)

    val description = "Au Brésil, un pan de falaise s'est détaché et a percuté des bateaux de touristes, samedi 8 janvier. Quelques minutes plus tôt, les touristes profitaient des décors sur le lac Furnas (Brésil). Soudain, un premier éboulement inquiète, suivi d'un autre. Les passagers d'un bateau tentent alors de donner l'alerte, en vain. Il est trop tard : un pan entier de la falaise s'effondre, et écrase les bateaux les plus proches. Le bilan est lourd : sept morts, et trois disparus. Les recherches ont repris Interrompues durant la nuit, les recherches se sont poursuivies dimanche. \"Nous pouvons voir maintenant des débris de bateau, qui ont été touchés dans l'accident\", a précisé l'un des pompiers. Deux heures avant la catastrophe, la protection civile avait recommandé d'éviter les cascades, en raison des fortes pluies. Une enquête devra déterminer si les compagnies de tourisme ont fait preuve de négligence."
    val news = News("Brésil : effondrement meurtrier d'une falaise sur un groupe de touristes", description, new Timestamp(new Date("01/09/2022").getTime), 1,
      "Laurent Delahousse", List("C.Verove", "O.Sauvayre"),
      "Elsa Pallot",
      List("Thibaud de Barbeyrac"),
      "http://localhost:8000/monde/bresil/bresil-effondrement-meurtrier-d-une-falaise-sur-un-groupe-de-touristes_4910403.html",
      "http://localhost:8000/replay.html",
      containsWordGlobalWarming = false,
        ParserFR2.FRANCE2)


    assert(news == listNews.head)
  }

  test("parseFrance2News") {
    val newsToHave = Await.result(ParserFR2.parseFrance2News(s"/one-day-tv-news-fr2.html", localhost),
      Duration(1, "minutes")
    ).flatten.filter(_.title == "Écoles : un nouveau protocole pour une rentrée marquée par l'incertitude")

    logger.info(s"listNews ${newsToHave}")
    val news = News("Écoles : un nouveau protocole pour une rentrée marquée par l'incertitude",
      "Mauvaise surprise pour les parents d'élèves d'une école de Poitiers (Vienne). Deux enseignantes sont absentes, positives au Covid-19, et l'une n'est pas remplacée. Une situation loin d'être isolée. Lundi 3 janvier à Paris, 13% des enseignants de primaire étaient absents. Les parents font donc l'école à la maison, tout en télétravaillant. Pour éviter cette situation, l'Éducation nationale promet d'embaucher des enseignants retraités et des vacataires. Guislaine David, cosecrétaire générale et porte-parole du SNUIPP-FSU, dénonce un manque de préparation. Protocole et gestes barrières Un établissement de région parisienne a décidé de maintenir toutes ses classes ouvertes, malgré une incertitude en fin de matinée. C'est un nouveau protocole pour les enseignants, mais aussi pour les élèves. Désormais, les enfants testés positifs resteront isolés 5 jours, puis feront un test. Pour revenir en classe, les cas contact devront présenter un test négatif, puis en refaire à J+2 et J+4. Un protocole qui s'accompagne d'une stricte application des gestes barrière.",
      new Timestamp(new Date("01/03/2022").getTime),
      1, "Anne-Sophie Lapix",
      List("S. Soubane", "J. Ricco", "M. Mullot", "C.-M. Denis", "B. Vignais", "L. Lavieille"),
       "Elsa Pallot",
      List("Franck Genauzeau"),
      "http://localhost:8000/sante/maladie/coronavirus/ecoles-un-nouveau-protocole-pour-une-rentree-marquee-par-l-incertitude_4903195.html",
      "http://localhost:8000/one-day-tv-news-fr2.html",
      containsWordGlobalWarming = false,
      ParserFR2.FRANCE2)

    assert(newsToHave.head == news)
  }

  test("parseDescriptionAuthors") {
    val (description, authors, editor, editorDeputy) = ParserFR2.parseDescriptionAuthors("/one-subject-tv-news-fr2.html", localhost)

    assert("Les centrales à gaz et les centrales nucléaires, bénéfiques au climat ? C’est une décision très controversée que s’apprête à adopter la Commission européenne. Elle propose en effet de classer le gaz et le nucléaire comme des énergies oeuvrant à la transition climatique, une position défendue par la France. \"On a besoin de toutes les énergies décarbonées pour lutter contre le réchauffement climatique\", avance Pascal Canfin, président de la commission Environnement au Parlement européen. Des pays anti-nucléaires vent debout contre la proposition Grâce à ce label, les investisseurs seraient encouragés à placer leur argent dans le gaz et le nucléaire. Mais certains pays anti-nucléaires, comme l’Allemagne, l’Autriche ou encore le Luxembourg, font entendre leur voix, et dénoncent une \"provocation\". \"On ne peut pas dire que le nucléaire est une énergie durable. N’allons pas investir de l’argent pour de nouvelles folies nucléaires\", annonce Damien Carème, eurodéputé. Pour que la mesure soit abandonnée, il faudrait que 20 pays s’y opposent." == description)
    assert(List("J.Gasparutto", "C.Vanpée", "H.Huet", "F.Ducobu", "S.Giaume", "S.Carter") == authors)
    assert("Elsa Pallot" == editor)
    assert(List("Thibaud de Barbeyrac") == editorDeputy)
  }

  test("parseDescriptionAuthors - old news 204/2013") {
    val (description, authors, editor, editorDeputy) = ParserFR2.parseDescriptionAuthors("/old-subject-tv-news-fr2.html", localhost)

    assert(List("") == authors)
    assert("" == editor)
    assert(List("") == editorDeputy)
    assert("Le régime d'Assad assure que les experts seront libres. Les inspecteurs des Nations Unies devrait entamer leurs investigations demain. L'objectif est de déterminer si, oui ou non, le régime a utilisé des armes chimiques. La présence militaire est renforcée en Méditerranée. Avec 4 navires de guerre déployées dans la zone, les USA ont décidé de placer la Syrie à porter de tirs. Va-t-on vers une intervention militaire occidentale contre le régime de Bachar al Assad? L'état major américain a présenté les options existantes pour intervenir. Nous avons envisagé toutes les options. La limite d'intervention est d'envoyer des milices. Les Occidentaux peuvent-ils obtenir un mandat de l'ONU pour une intervention ? Il faudrait que les Russes renoncent à utiliser leur droit de véto comme en 2011 lorsqu'ils avaient laisse voter l'intervention militaire en Lybie. Moscou ne semble pas prête à lâcher son allié Bachar al Assad. En témoigne ce communiqué. Les Russes incitent également les Américains à ne pas reproduire \"l'aventure de la guerre en lrak\". Même sans mandat de l'ONU, les Américains et leurs alliés pourraient décider d'intervenir comme en 1999 au Kosovo. Malgré l'opposition des Russes, il fallait stopper les massacres des Serbes de Milosevic. " == description)
  }
}
