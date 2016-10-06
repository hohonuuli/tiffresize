import java.io.File
import org.mbari.smith.tiffresize.{ TiffReader, JpegWriter }
import rx.lang.scala.Subject
import rx.lang.scala.schedulers.IOScheduler
import scala.util.control.NonFatal

object MainRX {

  val fileSubject = Subject[File]

  def main(args: Array[String]): Unit = {
    val src = new File(args(0))
    val target = new File(args(1))

    if (src.isDirectory) {

      var files = src.listFiles()
        .filter(isTiff)
        .sortBy(_.getAbsoluteFile().getName())
        .toList

      if (!target.exists()) {
        target.mkdirs()
      }

      // Process files on executor
      fileSubject.observeOn(IOScheduler())
        .foreach(f => resize(f, target, fileSubject))

      files.foreach(f => fileSubject.onNext(f))

    }
  }

  def isTiff(f: File): Boolean = {
    val uf = f.getAbsolutePath.toUpperCase
    uf.endsWith(".TIF") || uf.endsWith(".TIFF")
  }

  /**
   * Resize and image asycn
   */
  def resize(s: File, targetDir: File, fs: Subject[File]): Unit = {
    val n = s.getName
    val i = n.lastIndexOf(".")
    val simpleName = n.substring(0, i)
    val t = new File(targetDir, s"$simpleName.jpg")
    print(s"Converting $s to $t ... ")
    try {
      JpegWriter(TiffReader(s.getAbsolutePath), new File(t.getAbsolutePath))
      println("SUCCESS!!")
    } catch {
      case NonFatal(e) =>
        println("FAILED! An attempt to resize will be made again later.")
        fs.onNext(s)
    }
  }
}
