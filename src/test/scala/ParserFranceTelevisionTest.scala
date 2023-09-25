import com.github.polomarcus.html.ParserFranceTelevision
import com.github.polomarcus.html.ParserFranceTelevision.browser
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

class ParserFranceTelevisionTest extends AnyFunSuite {
  val localhost = "http://localhost:8000"
  test("parseFranceTelevisionHome") {
    val listNews =
      ParserFranceTelevision.parseFranceTelevisionHome(
        s"$localhost/home-tv-news-france-2.html",
        localhost)
    val news = News(
      "Pénurie de carburant : des avancées et des blocages dans le bras de fer entre Total et les syndicats",
      "",
      DateService.getTimestampFranceTelevision("le 14/10/2022"),
      1,
      "Laurent Delahousse",
      List("J. Bigard", "A. Boulet"),
      "Elsa Pallot",
      List("Sébastien Renout", "Anne-Charlotte Hinet", "Arnaud Comte"),
      "http://localhost:8000/one-subject-tv-news-fr2.html",
      "http://localhost:8000/replay.html",
      containsWordGlobalWarming = false,
      ParserFranceTelevision.FRANCE2)

    assert(news.title == listNews.head.title)
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
      subtitle == "La CGT adopte la stratégie du blocage en opposition à la CFDT qui, elle, revendique une belle négociation. Après avoir rejeté la proposition d’augmentation de 7 % de TotalEnergies, Philippe Martinez souhaiterait-il que le blocage perdure ?")
  }

  test("parseDescriptionAuthors") {
    val (description, authors, publishedDate) =
      ParserFranceTelevision
        .parseDescriptionAuthors("/one-subject-tv-news-fr2.html", localhost)
        .get

    assert(publishedDate == "le 14/10/2022 20:48")
    assert(
      "La CGT adopte la stratégie du blocage en opposition à la CFDT qui, elle, revendique une belle négociation. Après avoir rejeté la proposition d’augmentation de 7 % de TotalEnergies, Philippe Martinez souhaiterait-il que le blocage perdure ?Philippe Martinez arpente les piquets de grève et les plateaux de télévision depuis plusieurs jours, défendant les blocages. Et pour cause : malgré l’attente, les automobilistes ne grognent pas au sujet de la grève. “On a l’impression que les Français ont un regard sympathique sur la grève. Les ingrédients sont là pour que le mouvement prenne”, déclare un cadre de la CGT. Un dirigeant de la majorité dit comprendre la CGT, “qui a raison de pousser son avantage”. Le blocage pourrait s’étendre à d’autres secteurs Le patron de la CGT rejette toujours la proposition d’augmentation de 7% de TotalEnergies. Une ligne trop dure pour certains syndicats réformistes, quand d’autres y voient la proximité du congrès de la CGT dans quelques mois, où Philippe Martinez sera attendu au tournant. Un cadre d’un syndicat concurrent explique même qu’il “est pris au piège par ses fédérations les plus dures” Le mouvement pourrait s’étendre à d’autres secteurs, et des politiques à gauche le reprennent déjà. Le gouvernement ne semble pas craindre un nouveau mouvement à l’image de celui des gilets jaunes. Selon un proche du président, “les Français n’ont pas la tête à ça”." == description)
    assert(List("J. Bigard", "A. Boulet") == authors)
  }

  test("parseDescriptionAuthors - old news 204/2013") {
    val (description, authors, publishedDate) =
      ParserFranceTelevision
        .parseDescriptionAuthors("/old-subject-tv-news-fr2.html", localhost)
        .get

    assert(List("") == authors)
    assert(
      "Le régime d'Assad assure que les experts seront libres. Les inspecteurs des Nations Unies devrait entamer leurs investigations demain. L'objectif est de déterminer si, oui ou non, le régime a utilisé des armes chimiques. La présence militaire est renforcée en Méditerranée. Avec 4 navires de guerre déployées dans la zone, les USA ont décidé de placer la Syrie à porter de tirs. Va-t-on vers une intervention militaire occidentale contre le régime de Bachar al Assad? L'état major américain a présenté les options existantes pour intervenir. Nous avons envisagé toutes les options. La limite d'intervention est d'envoyer des milices. Les Occidentaux peuvent-ils obtenir un mandat de l'ONU pour une intervention ? Il faudrait que les Russes renoncent à utiliser leur droit de véto comme en 2011 lorsqu'ils avaient laisse voter l'intervention militaire en Lybie. Moscou ne semble pas prête à lâcher son allié Bachar al Assad. En témoigne ce communiqué. Les Russes incitent également les Américains à ne pas reproduire \"l'aventure de la guerre en lrak\". Même sans mandat de l'ONU, les Américains et leurs alliés pourraient décider d'intervenir comme en 1999 au Kosovo. Malgré l'opposition des Russes, il fallait stopper les massacres des Serbes de Milosevic. " == description)
  }

  test("getPresenter") {
    assert(
      ParserFranceTelevision.getPresenter(
        "Le JT de 20 Heures du samedi 8 octobre 2022 est présenté par Laurent Delahousse sur France 2. Retrouvez dans le journal télévisé du soir : la sélection des faits marquants, les interviews et témoignages, les invités politiques et")
        == "Laurent Delahousse")
  }
}
