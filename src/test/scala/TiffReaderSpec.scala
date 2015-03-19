import org.junit.runner.RunWith
import org.mbari.smith.tiffresize.TiffReader
import org.scalatest.{ Matchers, FlatSpec }

/**
 *
 *
 * @author Brian Schlining
 * @since 2015-03-19T10:18:00
 */
class TiffReaderSpec extends FlatSpec with Matchers {

  private[this] val tiffUrl = getClass.getResource("/StaM_0803_CameraSled_19910722_00_44_45.TIF")

  "TiffReader" should "read a tiff and return the correct size" in {
    val bufferedImage = TiffReader(tiffUrl.toExternalForm)
    bufferedImage should not be (null)

  }

}
