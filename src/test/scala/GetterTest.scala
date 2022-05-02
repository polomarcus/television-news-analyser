import com.github.polomarcus.html.{Getter, ParserTF1}
import com.github.polomarcus.model.News
import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

class GetterTest extends AnyFunSuite {
  test("getPagination") {
    assert(Getter.getPagination(2) == "/2.html")
    assert(Getter.getPagination(5) == "/5.html")
    assert(Getter.getPagination(1) == "")
  }
}
