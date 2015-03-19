package org.mbari.smith.tiffresize

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import org.imgscalr.Scalr

/**
 *
 *
 * @author Brian Schlining
 * @since 2015-03-19T10:51:00
 */
object JpegWriter {

  val PreferredWidth = 2650

  def apply(image: BufferedImage, file: File, width: Int = PreferredWidth): Boolean =
    write(resize(image, width), file)

  def resize(image: BufferedImage, width: Int): BufferedImage =
    Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, width)

  def write(bufferedImage: BufferedImage, file: File): Boolean = {
    try {
      val path = file.getAbsolutePath
      val idx = path.lastIndexOf(".")
      val ext = path.substring(idx + 1)
      ImageIO.write(bufferedImage, ext, file)
      true
    } catch {
      case e: Exception => false
    }
  }

}
