import com.github.polomarcus.utils.DateService
import com.github.polomarcus.utils.DateService.timezone
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{LocalDate, ZoneId}
import java.util.{Date, TimeZone}

class DateServiceTest extends AnyFunSuite {
  val timezone = TimeZone.getTimeZone("UTC+2")
  val format = new SimpleDateFormat("dd/MM/yyyy")
  format.setTimeZone(timezone)

  test("getTimestampFranceTelevision") {
    assert(
      DateService.getTimestampFranceTelevision("Diffusé le 08/01/2022") ==
        new Timestamp(format.parse("08/01/2022").getTime))
  }

  test("getTimestampTF1") {
    val formatTF1 = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    formatTF1.setTimeZone(timezone)
    assert(
      DateService.getTimestampTF1("Publié le 10 décembre 2020 à 20h08") ==
        new Timestamp(formatTF1.parse("10/12/2020 20:08").getTime))
    assert(DateService.getTimestampTF1("Publié hier à 20h39").getTime >= 0)
    assert(DateService.getTimestampTF1("Publié aujourd'hui à 20h39").getTime >= 0)
  }

  test("getHourMinute") {
    assert(DateService.getHourMinute("Publié le 10 décembre 2020 à 20h08") == ("20", "08"))
  }
}
