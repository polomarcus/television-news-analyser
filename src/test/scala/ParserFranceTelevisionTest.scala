import com.github.polomarcus.html
import com.github.polomarcus.html.Getter.logger
import com.github.polomarcus.html.ParserFranceTelevision
import com.github.polomarcus.html.ParserFranceTelevision.browser
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.time.LocalDate
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ParserFranceTelevisionTest extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseFranceTelevisionHome") {
    val listNews =
      ParserFranceTelevision.parseFranceTelevisionHome(
        s"$localhost/home-tv-news-france-2.html",
        localhost)

    val subtitle =
      "La chute d'un pan de falaise sur des bateaux, qui naviguaient sur un lac touristique brésilien, a fait sept morts et trois disparus, samedi 8 janvier. Le bilan est provisoire, et les recherches se poursuivent."
    val description =
      "Au Brésil, un pan de falaise s'est détaché et a percuté des bateaux de touristes, samedi 8 janvier. Quelques minutes plus tôt, les touristes profitaient des décors sur le lac Furnas (Brésil). Soudain, un premier éboulement inquiète, suivi d'un autre. Les passagers d'un bateau tentent alors de donner l'alerte, en vain. Il est trop tard : un pan entier de la falaise s'effondre, et écrase les bateaux les plus proches. Le bilan est lourd : sept morts, et trois disparus. Les recherches ont repris Interrompues durant la nuit, les recherches se sont poursuivies dimanche. \"Nous pouvons voir maintenant des débris de bateau, qui ont été touchés dans l'accident\", a précisé l'un des pompiers. Deux heures avant la catastrophe, la protection civile avait recommandé d'éviter les cascades, en raison des fortes pluies. Une enquête devra déterminer si les compagnies de tourisme ont fait preuve de négligence."
    val news = News(
      "Brésil : effondrement meurtrier d'une falaise sur un groupe de touristes",
      subtitle + description,
      DateService.getTimestampFranceTelevision("Diffusé le 09/01/2022"),
      1,
      "Laurent Delahousse",
      List("C.Verove", "O.Sauvayre"),
      "Elsa Pallot",
      List("Sébastien Renout", "Anne Poncinet", "Arnaud Comte"),
      "http://localhost:8000/monde/bresil/bresil-effondrement-meurtrier-d-une-falaise-sur-un-groupe-de-touristes_4910403.html",
      "http://localhost:8000/replay.html",
      containsWordGlobalWarming = false,
      ParserFranceTelevision.FRANCE2)

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
  }

  test("parseFranceTelevisionNews") {
    val newsToHave = Await
      .result(
        ParserFranceTelevision
          .parseFranceTelevisionNews(
            s"/one-day-tv-news-fr2.html",
            localhost,
            "France 2",
            "Elsa Pallot",
            List("Sébastien Renout", "Gilles Delbos")),
        Duration(1, "minutes"))
      .flatten
      .filter(
        _.title == "Écoles : un nouveau protocole pour une rentrée marquée par l'incertitude")

    logger.info(s"listNews ${newsToHave}")
    val news = News(
      "Écoles : un nouveau protocole pour une rentrée marquée par l'incertitude",
      "C'était un peu le saut dans l'inconnu, lundi 3 janvier, dans beaucoup d'écoles. Après deux semaines de vacances marquées par l'explosion de l'épidémie de Covid-19, on pouvait s'attendre à un absentéisme record, lundi 3 janvier, même si de nouvelles règles permettent de ne plus fermer les classes.Mauvaise surprise pour les parents d'élèves d'une école de Poitiers (Vienne). Deux enseignantes sont absentes, positives au Covid-19, et l'une n'est pas remplacée. Une situation loin d'être isolée. Lundi 3 janvier à Paris, 13% des enseignants de primaire étaient absents. Les parents font donc l'école à la maison, tout en télétravaillant. Pour éviter cette situation, l'Éducation nationale promet d'embaucher des enseignants retraités et des vacataires. Guislaine David, cosecrétaire générale et porte-parole du SNUIPP-FSU, dénonce un manque de préparation. Protocole et gestes barrières Un établissement de région parisienne a décidé de maintenir toutes ses classes ouvertes, malgré une incertitude en fin de matinée. C'est un nouveau protocole pour les enseignants, mais aussi pour les élèves. Désormais, les enfants testés positifs resteront isolés 5 jours, puis feront un test. Pour revenir en classe, les cas contact devront présenter un test négatif, puis en refaire à J+2 et J+4. Un protocole qui s'accompagne d'une stricte application des gestes barrière.",
      DateService.getTimestampFranceTelevision("Diffusé le 03/01/2022"),
      1,
      "Anne-Sophie Lapix",
      List("S. Soubane", "J. Ricco", "M. Mullot", "C.-M. Denis", "B. Vignais", "L. Lavieille"),
      "Elsa Pallot",
      List("Sébastien Renout", "Gilles Delbos"),
      "http://localhost:8000/sante/maladie/coronavirus/ecoles-un-nouveau-protocole-pour-une-rentree-marquee-par-l-incertitude_4903195.html",
      "http://localhost:8000/one-day-tv-news-fr2.html",
      containsWordGlobalWarming = false,
      ParserFranceTelevision.FRANCE2)

    assert(news.title == newsToHave.head.title)
    assert(news.description == newsToHave.head.description)
    assert(news.date == newsToHave.head.date)
    assert(news.order == newsToHave.head.order)
    assert(news.presenter == newsToHave.head.presenter)
    assert(news.authors == newsToHave.head.authors)
    assert(news.editor == newsToHave.head.editor)
    assert(news.editorDeputy == newsToHave.head.editorDeputy)
    assert(news.url == newsToHave.head.url)
    assert(news.urlTvNews == newsToHave.head.urlTvNews)
    assert(news.containsWordGlobalWarming == newsToHave.head.containsWordGlobalWarming)
    assert(news.media == newsToHave.head.media)
  }

  test("parseTeam") {
    val (editor, editorDeputy) =
      ParserFranceTelevision.parseTeam(noonNews = true, "http://localhost:8000/team-editor.html")
    assert("Thomas Horeau" == editor)
    assert(List("Régis Poullain", "Margaux Manière") == editorDeputy)
  }

  test("parseSubtitle") {
    val doc = browser.get("http://localhost:8000/one-subject-tv-news-fr2.html")

    val subtitle = ParserFranceTelevision.parseSubtitle(doc)
    assert(
      subtitle == "Bruxelles projette de leur apposer le label d’\"énergies vertes\", ce qui favoriserait les investissements dans ces énergies et permettrait d’atteindre la neutralité carbone.")
  }

  test("parseDescriptionAuthors") {
    val (description, authors) =
      ParserFranceTelevision.parseDescriptionAuthors("/one-subject-tv-news-fr2.html", localhost)

    assert(
      "Bruxelles projette de leur apposer le label d’\"énergies vertes\", ce qui favoriserait les investissements dans ces énergies et permettrait d’atteindre la neutralité carbone.Les centrales à gaz et les centrales nucléaires, bénéfiques au climat ? C’est une décision très controversée que s’apprête à adopter la Commission européenne. Elle propose en effet de classer le gaz et le nucléaire comme des énergies oeuvrant à la transition climatique, une position défendue par la France. \"On a besoin de toutes les énergies décarbonées pour lutter contre le réchauffement climatique\", avance Pascal Canfin, président de la commission Environnement au Parlement européen. Des pays anti-nucléaires vent debout contre la proposition Grâce à ce label, les investisseurs seraient encouragés à placer leur argent dans le gaz et le nucléaire. Mais certains pays anti-nucléaires, comme l’Allemagne, l’Autriche ou encore le Luxembourg, font entendre leur voix, et dénoncent une \"provocation\". \"On ne peut pas dire que le nucléaire est une énergie durable. N’allons pas investir de l’argent pour de nouvelles folies nucléaires\", annonce Damien Carème, eurodéputé. Pour que la mesure soit abandonnée, il faudrait que 20 pays s’y opposent." == description)
    assert(
      List("J.Gasparutto", "C.Vanpée", "H.Huet", "F.Ducobu", "S.Giaume", "S.Carter") == authors)
  }

  test("parseDescriptionAuthors - old news 204/2013") {
    val (description, authors) =
      ParserFranceTelevision.parseDescriptionAuthors("/old-subject-tv-news-fr2.html", localhost)

    assert(List("") == authors)
    assert(
      "Le régime d'Assad assure que les experts seront libres. Les inspecteurs des Nations Unies devrait entamer leurs investigations demain. L'objectif est de déterminer si, oui ou non, le régime a utilisé des armes chimiques. La présence militaire est renforcée en Méditerranée. Avec 4 navires de guerre déployées dans la zone, les USA ont décidé de placer la Syrie à porter de tirs. Va-t-on vers une intervention militaire occidentale contre le régime de Bachar al Assad? L'état major américain a présenté les options existantes pour intervenir. Nous avons envisagé toutes les options. La limite d'intervention est d'envoyer des milices. Les Occidentaux peuvent-ils obtenir un mandat de l'ONU pour une intervention ? Il faudrait que les Russes renoncent à utiliser leur droit de véto comme en 2011 lorsqu'ils avaient laisse voter l'intervention militaire en Lybie. Moscou ne semble pas prête à lâcher son allié Bachar al Assad. En témoigne ce communiqué. Les Russes incitent également les Américains à ne pas reproduire \"l'aventure de la guerre en lrak\". Même sans mandat de l'ONU, les Américains et leurs alliés pourraient décider d'intervenir comme en 1999 au Kosovo. Malgré l'opposition des Russes, il fallait stopper les massacres des Serbes de Milosevic. " == description)
  }
}
