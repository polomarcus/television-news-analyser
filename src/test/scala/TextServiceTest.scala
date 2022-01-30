import com.github.polomarcus.utils.TextService
import org.scalatest.funsuite.AnyFunSuite

class TextServiceTest extends AnyFunSuite {
    test("containsWordGlobalWarming") {
      assert(!TextService.containsWordGlobalWarming("pizza"))
    }

    test("containsWordGlobalWarming - aléas climatiques") {
      assert(!TextService.containsWordGlobalWarming("aléas climatiques"))
    }

    test("réchauffement") {
      assert(TextService.containsWordGlobalWarming("réchauffement climatique"))
    }

    test("dérèglement") {
      assert(TextService.containsWordGlobalWarming("dérèglement climatique"))
    }

    test("changement") {
      assert(TextService.containsWordGlobalWarming("changement climatique"))
    }

    test("changements") {
      assert(TextService.containsWordGlobalWarming("changements climatiques"))
    }

    test("changements with capital letters") {
      assert(TextService.containsWordGlobalWarming("Les Changements Climatiques"))
    }
}
