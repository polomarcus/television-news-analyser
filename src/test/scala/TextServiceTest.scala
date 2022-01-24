import com.github.polomarcus.utils.TextService
import org.scalatest.funsuite.AnyFunSuite

class TextServiceTest extends AnyFunSuite {
  test("containsWordGlobalWarming") {
    assert(TextService.containsWordGlobalWarming("pizza") == false)
    assert(TextService.containsWordGlobalWarming("réchauffement climatique") == true)
    assert(TextService.containsWordGlobalWarming("changement climatique") == true)
  }
}
