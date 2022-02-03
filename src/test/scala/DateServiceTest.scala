import com.github.polomarcus.utils.DateService
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.time.{LocalDate, ZoneId}
import java.util.Date

class DateServiceTest extends AnyFunSuite {
  test("getTimestampFrance2") {
    assert(DateService.getTimestampFrance2  ("Diffusé le 08/01/2022") == new Timestamp(new Date("01/08/2022").getTime))
  }

  test("getTimestampTF1") {
    assert(DateService.getTimestampTF1  ("Publié le 10 décembre 2020 à 20h08") == new Timestamp(new Date("12/10/2020").getTime))
    assert(DateService.getTimestampTF1  ("Publié hier à 20h39").getTime >= 0)
    assert(DateService.getTimestampTF1  ("Publié aujourd'hui à 20h39").getTime >= 0)
  }
}
