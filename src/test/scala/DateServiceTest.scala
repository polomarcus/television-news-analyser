import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

import java.util.Date

class DateServiceTest extends AnyFunSuite {
  test("getTimestampFrance2") {
    assert(DateService.getTimestampFrance2  ("Diffusé le 08/01/2022") == new Date("01/08/2022"))
  }
}
