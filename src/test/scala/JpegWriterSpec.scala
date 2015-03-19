import java.io.File
import javax.imageio.ImageIO

import org.mbari.smith.tiffresize.{ JpegWriter, TiffReader }
import org.scalatest.{ Matchers, FlatSpec }

/**
 *
 *
 * @author Brian Schlining
 * @since 2015-03-19T11:00:00
 */
class JpegWriterSpec extends FlatSpec with Matchers {

  private[this] val tiffUrl = getClass.getResource("/StaM_0803_CameraSled_19910722_00_44_45.TIF")

  "JpegWriter" should "correctly scale and write an image" in {
    val target = new File(s"target/${getClass.getSimpleName}.jpg")
    val bi = TiffReader(tiffUrl.toExternalForm)
    val ok = JpegWriter(bi, target, JpegWriter.PreferredWidth)
    ok should be(true)
    val jpg = ImageIO.read(target)
    jpg should not be (null)
    jpg.getWidth should be(JpegWriter.PreferredWidth)
  }

}
