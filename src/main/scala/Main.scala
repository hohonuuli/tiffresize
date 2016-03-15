import java.io.File
import org.mbari.smith.tiffresize.{ TiffReader, JpegWriter }
import scala.util.Try

object Main extends App {
  val src = new File(args(0))
  val target = new File(args(1))

  if (src.isDirectory) {

    def isTiff(f: File): Boolean = {
      val uf = f.getAbsolutePath.toUpperCase
      uf.endsWith(".TIF") || uf.endsWith(".TIFF")
    }

    val files = src.listFiles()
      .filter(isTiff)
      .sortBy(_.getAbsoluteFile().getName())

    if (!target.exists()) {
      target.mkdirs()
    }

    for (s <- files) {
      val n = s.getName
      val i = n.lastIndexOf(".")
      val simpleName = n.substring(0, i)
      val t = new File(target, s"$simpleName.jpg")
      print(s"Converting $s to $t ... ")
      val ok = Try(JpegWriter(TiffReader(s.getAbsolutePath), new File(t.getAbsolutePath))).getOrElse(false)
      if (ok) {
        println("success")
      } else {
        println("FAILED!")
      }
    }
  } else {
    JpegWriter(TiffReader(src.getAbsolutePath), new File(target.getAbsolutePath))
  }

}
