package org.mbari.smith.tiffresize

import java.awt.image.{ RenderedImage, BufferedImage }
import java.io.File
import java.net.URL
import javax.media.jai.JAI
import org.mbari.awt.image.ImageUtilities

/**
 *
 *
 * @author Brian Schlining
 * @since 2015-03-19T10:14:00
 */
object TiffReader {

  def apply(location: String): BufferedImage = toBufferedImage(read(location))

  def read(location: String): RenderedImage = {
    val url = if (location.startsWith("http") || location.startsWith("file")) new URL(location)
    else new File(location).toURI.toURL
    JAI.create("url", url)
  }

  def toBufferedImage(image: RenderedImage): BufferedImage = ImageUtilities.toBufferedImage(image)

  // -- OpenIMAG
  //  def read(location: String): MBFImage = {
  //    if (location.startsWith("file:") || location.startsWith("http")) {
  //      ImageUtilities.readMBF(new URL(location))
  //    } else {
  //      ImageUtilities.readMBF(new File(location))
  //    }
  //  }
  //
  //  def toBufferedImage(image: MBFImage): BufferedImage =
  //    ImageUtilities.createBufferedImageForDisplay(image)

}
