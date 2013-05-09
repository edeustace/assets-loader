package com.ee.assets.processors

import org.specs2.mutable.Specification
import java.io.File
import scala.xml.{Node, Elem}

import com.ee.assets.models._
import org.specs2.matcher.{MatchSuccess, MatchResult}
import org.specs2.specification.{Fragments, Step}

class SimpleFileProcessorTest extends MockTargetFolder {

  def targetRoot: String = "test/mock-target"

  def targetFolder: String = "test/mock-target/test/public/com/ee/assets"

  def contentsPath: String = "test/public/com/ee/assets/processors"

  helpers.PlaySingleton.start

  "Simple File Processor" should {

    def assertSrc(xml: Elem, sources: String*): MatchResult[Any] = {
      println("assertSrc")
      println(xml)
      val lengthAssertion = (xml \\ "script").length === sources.length
      val assertions = (xml \\ "script").toList.zipWithIndex.map {
        t: (Node, Int) =>
          (t._1 \ "@src").text === sources(t._2)
      }

      (assertions :+ lengthAssertion).filterNot {
        r => r match {
          case MatchSuccess(_, _, _) => true
          case _ => false
        }
      }.length === 0
    }

    def path(s: String*) = s.mkString("/")

    "when working with a folder and a path" in {

      import com.ee.utils.file
      val relativeRoot = "com/ee/assets/processors/testFileAndFolder"
      val assetsPath = "test/public"
      val assetsFilePath = path(assetsPath, relativeRoot)
      val assetInfo = new AssetsInfo("/webpath", assetsPath)
      val names = List("folder_one", "root_one.js")
      val rawFiles = names.map(path(assetsFilePath, _)).map(new File(_))
      val files = file.distinctFiles(rawFiles: _*)
      val hash = file.hash(files)

      def assertConfig(c: AssetsLoaderConfig, expectedFiles: String*): org.specs2.execute.Result = {
        val processor = new SimpleFileProcessor(assetInfo, c, "test/mock-target")
        val out = processor.process("test",files)
        println(out)
        val xml = scala.xml.XML.loadString("<head>" + out.mkString("\n") + "</head>")
        (xml \\ "script").length === expectedFiles.length
        assertSrc(xml, expectedFiles: _*)
      }

      "work" in {
        assertConfig(
          AssetsLoaderConfig(false, false, false),
          "/webpath/" + relativeRoot + "/folder_one/one.js",
          "/webpath/" + relativeRoot + "/root_one.js"
        )
      }

      "concat" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = true, false, false),
          "/webpath/test-" + hash + ".js")
      }

      "minify - but don't concat" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = false, minify = true, gzip = false),
          "/webpath/" + relativeRoot + "/folder_one/one.min.js",
          "/webpath/" + relativeRoot + "/root_one.min.js"
        )
      }

      "gzip - but don't concat" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = false, minify = false, gzip = true),
          "/webpath/" + relativeRoot + "/folder_one/one.gz.js",
          "/webpath/" + relativeRoot + "/root_one.gz.js"
        )
      }

      "minify + gzip - but don't concat" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = false, minify = true, gzip = true),
          "/webpath/" + relativeRoot + "/folder_one/one.min.gz.js",
          "/webpath/" + relativeRoot + "/root_one.min.gz.js"
        )
      }

      "minify" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = true, minify = true, false),
          "/webpath/test-" + hash + ".min.js")
      }

      "gzip" in {
        assertConfig(
          AssetsLoaderConfig(concatenate = true, minify = true, gzip = true),
          "/webpath/test-" + hash + ".min.gz.js")
      }
    }
  }

}

trait MockTargetFolder extends Specification {

  import scala.sys.process._

  def targetRoot : String
  def targetFolder: String

  def contentsPath: String

  override def map(fs: => Fragments) = Step(before) ^ fs ^ Step(after)

  def before {
    //Note - using the full syntax here so we don't conflict with specs2
    println("copying over to test target dir..")
    val mkdir: String = "mkdir -p " + targetFolder
    stringToProcess(mkdir).run()
    val cpR: String = "cp -r " + contentsPath + " " + targetFolder
    stringToProcess(cpR).run()
  }

  def after {
    println("removing target dir.")
    val rm = "rm -fr " + targetRoot
    stringToProcess(rm).run()
  }
}

