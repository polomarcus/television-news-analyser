import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.util.Date

class DateServiceTest extends AnyFunSuite {
  test("getTimestampFrance2") {
    assert(DateService.getTimestampFrance2  ("Diffusé le 08/01/2022") == new Timestamp(new Date("01/08/2022").getTime))
  }
}
