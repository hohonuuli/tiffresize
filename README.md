# tiffresize

A program to batch convert git TIF images into smaller (2560 pixel wide) but high-quality JPG images.

## Running

You can build an executable jar with:

```bash
sbt assembly
```

The output from that command will point you at the executable jar.

### Convert a single file

```bash
java -Xmx4g -jar tiffresize.jar [TIFF] [JPG]

# Example
java -Xmx4g -jar tiffresize.jar path/to/StaM_0803_CameraSled_19910722_00_34_40.TIF newpath/to/StaM_0803_CameraSled_19910722_00_34_40.jpg

```


### Convert a directory of files

```bash
java -Xmx4g -jar [TIFF Directory] [JPG Directory]

# Example
java -Xmx4g -jar path/to/tiffs newpath/to/jpgs

```

## What has gone before
1. Tried imagemagick, it can't read the tiffs.
2. Tried [OpenIMAJ](http://www.openimaj.org/) - It did not work. Throws `java.io.IOException: Resetting to invalid mark`
3. Looked at [imglib2](https://github.com/imglib/imglib2) - Not sure how to do _anything_ with it. The docs needs some love.
4. [ifranview](http://www.irfanview.com/) works but generates 25MB JPG ... WTF?
5. Using [JAI](http://www.oracle.com/technetwork/java/javase/tech/jai-142803.html) and [imgscalr](https://github.com/thebuzzmedia/imgscalr), seems to work fine


This project is built using [SBT](http://www.scala-sbt.org/)

## Useful [SBT commands](http://www.scala-sbt.org/release/docs/Command-Line-Reference.html) for this project

- `checkVersions`: Show dependency updates
- `clean`
- `cleanall`: Does `clean` and `clean-files`
- `compile` or `~compile` (continuous)
- `console`: Opens a scala console that includes the projects dependencies and code on the classpath
- `dependency-tree`: Shows an ASCII dependency graph
- `dependencyUpdates`: Show dependency updates
- `doc`: Generate Scaladoc into target/api
- `export fullClasspath`: Generate the classpath needed to run the project
- `install`
- `ivy-report`: build a report of dependencies using ivy in XML (viewable in a browser)
- `lint:compile`: Run static checkers as part of compilion. (Static checking is slow)
- `offline`: Use SBT offline
- `pack`: Builds a standalone distribution of this project under `target/pack`
- `pack-archive`: Takes the product from `pack` and generates a _tar.gz_ archive
- `package`: Creates the main artifact (e.g. a jar) under `target`
- `publish-local` or `~publish-local` (continous): Publish to the local ivy repo
- `publishM2`: Publish to the local maven repo
- `reload`: Reloads the build. Useful if you edit build.sbt.
_ `scalastyleGenerateConfig`: Generates a scalastyle config file. Run before using `scalastyle`
- `scalastyle`: Checks code style. Results go into target/scalastyle-result.xml. Also `test:scalastyle`
- `show ivy-report`: Show the location of the dependency report
- `show update`: Show dependencies and indicate which were evicted
- `tasks -V`: Shows all available tasks/commands
- `test` or `~test` (continuous)
- `update-classifiers`: Download sources and javadoc for all dependencies
- `version-report`: Shows a flat listing of all dependencies in this project, including transitive ones.

## To run main class using SBT to launch it
`sbt 'run-main org.mbari.foo.Main'`

## To run a single test
`sbt 'test-only org.mbari.foo.ExampleSpec'`

## References
[Best Practices](https://github.com/alexandru/scala-best-practices/)
[SBT Pack](https://github.com/xerial/sbt-pack) - Files added in `src/pack` are included in product

