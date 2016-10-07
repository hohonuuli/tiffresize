import java.io.File
import java.util.concurrent.{ ExecutorService, Executors, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

import org.mbari.smith.tiffresize.{ FileInfo, JpegWriter, TiffReader }
import rx.lang.scala.{ Scheduler, Subject }
import rx.lang.scala.schedulers.{ ComputationScheduler, ExecutionContextScheduler, IOScheduler }

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object MainRxFast {

  val readSubject = Subject[FileInfo]
  val writeSubject = Subject[FileInfo]

  val completedCount = new AtomicInteger(0)

  def main(args: Array[String]): Unit = {
    val src = new File(args(0))
    val target = new File(args(1))

    if (src.isDirectory) {

      var files = src.listFiles()
        .filter(Functions.isTiff)
        .sortBy(_.getAbsoluteFile.getName())
        .map(FileInfo(_, 0))
        .toList

      if (!target.exists()) {
        target.mkdirs()
      }

      // Process files on executor
      //val io = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2)))
      //val io = IOScheduler()

      val ioIn = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4)))
      val ioOut = ioIn

      readSubject
        .onBackpressureBuffer
        .observeOn(ioIn)
        .foreach(f => read(f, target, readSubject, writeSubject))

      writeSubject.observeOn(ioOut)
        .foreach(f => write(f, target, readSubject))

      // We have to throttle this back or all retries will happen in immediate succession
      files.foreach(f => readSubject.onNext(f))

      while (true) {
        Thread.sleep(333)
        if (completedCount.get() == files.size) {
          System.exit(0)
        }
      }

    }
  }

  private def retry(fi: FileInfo, readSubject: Subject[FileInfo], msg: String): Unit = {
    if (fi.count < 4) {
      println(s"$msg FAILED! Trying again later.")
      readSubject.onNext(fi.increment)
    } else {
      completedCount.getAndIncrement()
      println(s"$msg FAILED!!. We tried 5 times. We're giving up!")
    }
  }

  private def read(
    fi: FileInfo,
    targetDir: File,
    readSubject: Subject[FileInfo],
    writeSubject: Subject[FileInfo]
  ): Unit = {

    val t = Functions.sourceFileToTargetFile(fi.file, targetDir)
    if (t.exists()) {
      completedCount.getAndIncrement()
      println(s"[${Thread.currentThread().getName}] Skipping ${fi.file}, JPG already exists in ${targetDir}")
    } else {
      val msg = s"[${Thread.currentThread().getName}] Reading ${fi.file} ..."
      try {
        val img = TiffReader(fi.file.getAbsolutePath)
        val smallImg = JpegWriter.resize(img, JpegWriter.PreferredWidth)
        val fiWithImg = fi.copy(image = Some(smallImg))
        println(s"$msg SUCCESS!")
        writeSubject.onNext(fiWithImg)
      } catch {
        case NonFatal(e) => retry(fi, readSubject, msg)
      }
    }

  }

  private def write(
    fi: FileInfo,
    targetDir: File,
    readSubject: Subject[FileInfo]
  ): Unit = {

    val t = Functions.sourceFileToTargetFile(fi.file, targetDir)

    val msg = s"[${Thread.currentThread().getName}] Writing $t ..."

    try {
      fi.image match {
        case None => retry(fi, readSubject, msg)
        case Some(img) =>
          JpegWriter.write(img, t)
          println(s"$msg SUCCESS!")
      }
    } catch {
      case NonFatal(e) => retry(fi, readSubject, msg)
    }

  }

}

