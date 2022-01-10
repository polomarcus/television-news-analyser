import org.scalatest.FunSuite
import com.github.polomarcus.utils.DateService

class DateServiceTest extends FunSuite {


  test("getTimestamp") {
    assert(DateService.getTimestamp("2018/02/20 10:45") == 1519119900)
  }
}
