import com.github.polomarcus.html.{Getter, Parser}
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.FutureService
import org.scalatest.FunSuite

import java.time.LocalDate
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ParserTest extends FunSuite {

  test("parseFrance2News") {
    val listNews = Await.result(Parser.parseFrance2News("http://127.0.0.1:8080/src/test/resources/home-tv-news-fr2.html"),
      Duration(20, "minutes")
    )

    val song = News("My title", "description", new Date(), 3)

    assert(song == listNews.head)
  }
}
