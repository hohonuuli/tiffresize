import java.io.File
import java.util.concurrent.{ ExecutorService, Executors, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

import org.mbari.smith.tiffresize.{ FileInfo, JpegWriter, TiffReader }
import rx.lang.scala.{ Scheduler, Subject }
import rx.lang.scala.schedulers.{ ComputationScheduler, ExecutionContextScheduler, IOScheduler }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object MainRxPar {

  val readSubject = Subject[FileInfo]
  val resizeSubject = Subject[FileInfo]
  val writeSubject = Subject[FileInfo]

  val completedCount = new AtomicInteger

  def main(args: Array[String]): Unit = {
    val src = new File(args(0))
    val target = new File(args(1))

    if (src.isDirectory) {

      var files = src.listFiles()
        .filter(Functions.isTiff)
        .sortBy(_.getAbsoluteFile().getName())
        .map(FileInfo(_, 0))
        .toList

      if (!target.exists()) {
        target.mkdirs()
      }

      // Process files on executor
      //val io = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2)))
      //val io = IOScheduler()

      val ioIn = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1)))
      val ioOut = ExecutionContextScheduler(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2)))
      val compute = ComputationScheduler()

      readSubject
        .onBackpressureBuffer
        .observeOn(ioIn)
        .foreach(f => read(f, readSubject, resizeSubject))

      resizeSubject.observeOn(compute)
        .foreach(f => resize(f, readSubject, writeSubject))

      writeSubject.observeOn(ioOut)
        .foreach(f => write(f, target, readSubject))

      // We have to throttle this back or all retries will happen in immediate succession
      files.foreach(f => readSubject.onNext(f))

      var ok = true
      while (ok) {
        Thread.sleep(100)
        if (completedCount.get() == files.size) {
          ok = false
        }
      }

    }
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

  def read(
    fi: FileInfo,
    readSubject: Subject[FileInfo],
    computeSubject: Subject[FileInfo]
  ): Unit = {

    val msg = s"[${Thread.currentThread().getName}] Reading ${fi.file} ..."
    try {
      val img = TiffReader(fi.file.getAbsolutePath)
      val fiWithImg = fi.copy(image = Some(img))
      println(s"$msg SUCCESS!")
      computeSubject.onNext(fiWithImg)
    } catch {
      case NonFatal(e) => retry(fi, readSubject, msg)
    }
  }

  def resize(
    fi: FileInfo,
    readSubject: Subject[FileInfo],
    writeSubject: Subject[FileInfo]
  ): Unit = {

    val msg = s"[${Thread.currentThread().getName}] Resizing ${fi.file} ..."

    try {
      fi.image match {
        case None => retry(fi, readSubject, msg)
        case Some(img) =>
          val smallImg = JpegWriter.resize(img, JpegWriter.PreferredWidth)
          val fiWithSmallImg = fi.copy(image = Option(smallImg))
          println(s"$msg SUCCESS!")
          writeSubject.onNext(fiWithSmallImg)
      }
    } catch {
      case NonFatal(e) => retry(fi, readSubject, msg)
    }
  }

  def write(
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
          completedCount.getAndIncrement()
          println(s"$msg SUCCESS!")
      }
    } catch {
      case NonFatal(e) => retry(fi, readSubject, msg)
    }

  }

}

