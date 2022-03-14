import com.github.polomarcus.utils.TextService
import org.scalatest.funsuite.AnyFunSuite

class TextServiceTest extends AnyFunSuite {
  test("containsWordGlobalWarming") {
    assert(!TextService.containsWordGlobalWarming("pizza"))
  }

  test("containsWordGlobalWarming - does not count aléas climatiques") {
    assert(!TextService.containsWordGlobalWarming("aléas climatiques"))
  }

  test("containsWordGlobalWarming - does not count just 'climat'") {
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

  test("with 'du climat'") {
    assert(TextService.containsWordGlobalWarming("dérèglement du climat"))
    assert(TextService.containsWordGlobalWarming("réchauffement du climat"))
    assert(TextService.containsWordGlobalWarming("changement du climat"))
  }

  test("enjeux climatiques") {
    assert(TextService.containsWordGlobalWarming("enjeux climatiques"))
  }

  test("crise climatique") {
    assert(TextService.containsWordGlobalWarming("crise climatique"))
    assert(TextService.containsWordGlobalWarming("crises climatiques"))
  }
}
