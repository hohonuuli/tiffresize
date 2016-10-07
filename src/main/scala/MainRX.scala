import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.mbari.smith.tiffresize.{ FileInfo, JpegWriter, TiffReader }
import rx.lang.scala.Subject
import rx.lang.scala.schedulers.{ ComputationScheduler, ExecutionContextScheduler, IOScheduler }

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object MainRx {

  val fileSubject = Subject[FileInfo]
  val completedCount = new AtomicInteger

  def main(args: Array[String]): Unit = {
    val src = new File(args(0))
    val target = new File(args(1))

    if (src.isDirectory) {

      var files = src.listFiles()
        .filter(isTiff)
        .sortBy(_.getAbsoluteFile.getName())
        .map(FileInfo(_, 0))
        .toList

      if (!target.exists()) {
        target.mkdirs()
      }

      // Process files on executor
      //fileSubject.subscribeOn(ExecutionContextScheduler(ExecutionContext.global))
      val io = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4)))

      fileSubject.onBackpressureBuffer
        .observeOn(io)
        .foreach(f => resize(f, target, fileSubject))

      files.foreach(f => fileSubject.onNext(f))

      var ok = true
      while (ok) {
        Thread.sleep(100)
        if (completedCount.get() == files.size) {
          ok = false
        }
      }

    }
  }

  def isTiff(f: File): Boolean = {
    val uf = f.getAbsolutePath.toUpperCase
    uf.endsWith(".TIF") || uf.endsWith(".TIFF")
  }

  def retry(fi: FileInfo, readSubject: Subject[FileInfo], msg: String): Unit = {
    if (fi.count < 4) {
      println(s"$msg FAILED! Trying again later.")
      readSubject.onNext(fi.increment)
    } else {
      completedCount.getAndIncrement()
      println(s"$msg FAILED!!. We tried 5 times. We're giving up!")
    }
  }

  /**
   * Resize and image asycn
   */
  def resize(s: FileInfo, targetDir: File, fs: Subject[FileInfo]): Unit = {
    val n = s.file.getName
    val i = n.lastIndexOf(".")
    val simpleName = n.substring(0, i)
    val t = new File(targetDir, s"$simpleName.jpg")
    val msg = s"[${Thread.currentThread().getName}] Converting ${s.file} to $t ..."
    try {
      JpegWriter(TiffReader(s.file.getAbsolutePath), new File(t.getAbsolutePath))
      completedCount.getAndIncrement()
      println(s"$msg SUCCESS!!")
    } catch {
      case NonFatal(e) => retry(s, fs, msg)
    }
  }
}

