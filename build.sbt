// PROJECT PROPERTIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Implements strict static analysis recommended at
// https://docs.google.com/presentation/d/1tCmphnyP3F5WUtd1iNLub0TWRVyfTJsqhvzYhNS41WY/pub?start=false#slide=id.p

organization in ThisBuild := "org.mbari.smith"

name := "tiffresize"

version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

mainClass in assembly := Some("MainRxFast")

// https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Xfuture")
  //"-Ywarn-unused-import")     // 2.11 only. Really SLOW!!

javacOptions in ThisBuild ++= Seq("-target", "1.8", "-source","1.8")

// DEPENDENCIES ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

updateOptions := updateOptions.value.withCachedResolution(true)

// Add SLF4J, Logback and testing libs
libraryDependencies ++= {
  val slf4jVersion = "1.7.21"
  val logbackVersion = "1.1.7"
  val imglib2Version = "2.2.1"
  val jaiVersion = "1.1.3"
  val openimajVersion = "1.3.1"
  Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion,
    "io.reactivex" %% "rxscala" % "0.26.2",
    "javax.media.jai" % "com.springsource.javax.media.jai.core" % jaiVersion,
    "javax.media.jai" % "com.springsource.javax.media.jai.codec" % jaiVersion,
    "junit" % "junit" % "4.12" % "test",
    //"net.imglib2" % "imglib2" % openimajVersion,
    //"org.openimaj" % "image-processing" % openimajVersion,
    "org.imgscalr" % "imgscalr-lib" % "4.2",
    "org.mbari" % "mbarix4j" % "1.9.2",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
    "org.slf4j" % "slf4j-api" % slf4jVersion)
}

resolvers in ThisBuild ++= Seq(Resolver.mavenLocal,
    "mbari-maven-repository" at "https://mbari-maven-repository.googlecode.com/svn/repository")

//publishMavenStyle := true

//publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// OTHER SETTINGS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state =>
  val user = System.getProperty("user.name")
  user + "@" + Project.extract(state).currentRef.project + ":sbt> "
}

// Add this setting to your project to generate a version report (See ExtendedBuild.scala too.)
// Use as 'sbt versionReport' or 'sbt version-report'
versionReport <<= (externalDependencyClasspath in Compile, streams) map {
  (cp: Seq[Attributed[File]], streams) =>
    val report = cp.map {
      attributed =>
        attributed.get(Keys.moduleID.key) match {
          case Some(moduleId) => "%40s %20s %10s %10s".format(
            moduleId.organization,
            moduleId.name,
            moduleId.revision,
            moduleId.configurations.getOrElse("")
          )
          case None =>
            // unmanaged JAR, just
            attributed.data.getAbsolutePath
        }
    }.sortBy(a => a.trim.split("\\s+").map(_.toUpperCase).take(2).last).mkString("\n")
    streams.log.info(report)
    report
}

// For sbt-pack
packAutoSettings

// -- SCALARIFORM
// Format code on save with scalariform
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.scalariformSettings

SbtScalariform.ScalariformKeys.preferences := SbtScalariform.ScalariformKeys.preferences.value
  .setPreference(IndentSpaces, 2)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
  .setPreference(DoubleIndentClassDeclaration, true)

// fork a new JVM for run and test:run
fork := true

// Aliases
addCommandAlias("cleanall", ";clean;clean-files")

initialCommands in console :=
  """
    |import java.util.Date
  """.stripMargin
