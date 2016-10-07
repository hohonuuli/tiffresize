package org.mbari.smith.tiffresize

import java.awt.image.BufferedImage
import java.io.File

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-10-07T10:14:00
 */
case class FileInfo(file: File, count: Int, image: Option[BufferedImage] = None) {
  def increment: FileInfo = this.copy(count = this.count + 1)
}
