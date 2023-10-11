import com.github.polomarcus.html.Getter
import org.scalatest.funsuite.AnyFunSuite

class GetterTest extends AnyFunSuite {
  test("getPagination") {
    assert(Getter.getPagination(2, "france2") == "/2.html")
    assert(Getter.getPagination(5, "france2") == "/5.html")
    assert(Getter.getPagination(5, "tf1") == "/5")
    assert(Getter.getPagination(1, "france3") == "")
    assert(Getter.getPagination(1, "tf1") == "")
  }
}
