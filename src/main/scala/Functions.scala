import java.io.File

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-10-07T13:51:00
 */
object Functions {

  def isTiff(f: File): Boolean = {
    val uf = f.getAbsolutePath.toUpperCase
    uf.endsWith(".TIF") || uf.endsWith(".TIFF")
  }

  def sourceFileToTargetFile(f: File, targetDir: File): File = {
    val n = f.getName
    val i = n.lastIndexOf(".")
    val simpleName = n.substring(0, i)
    new File(targetDir, s"$simpleName.jpg")
  }

}
