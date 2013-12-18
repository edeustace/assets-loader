package com.ee.assets.processors

import com.ee.assets.Loader
import com.ee.assets.deployment.{Deployer, NullDeployer}
import com.ee.assets.models._
import java.io.File
import org.specs2.matcher.{MatchSuccess, MatchResult}
import org.specs2.mutable.Specification
import org.specs2.specification.{Scope, Fragments, Step}
import scala.xml.{Node, Elem}
import play.api.{Mode, Configuration}
import grizzled.file.util


class SimpleFileProcessorTest extends MockTargetFolder {

  def targetRoot: String = "test/mock-target"

  def targetFolder: String = "test/mock-target/test/public/com/ee/assets"

  def contentsPath: String = "test/public/com/ee/assets/"

  helpers.PlaySingleton.start

  val loader = new Loader(mode = Mode.Dev, config = Configuration.empty)

  import com.ee.utils.file

  val relativeRoot = "com/ee/assets/processors/testFileAndFolder"
  val assetsPath = "test/public"
  val assetsFilePath = path(assetsPath, relativeRoot)
  val assetInfo = new AssetsInfo("/webpath", assetsPath)
  val names = List("folder_one", "root_one.js")
  val rawFiles = names.map(path(assetsFilePath, _)).map(new File(_))
  val files = file.distinctFiles(rawFiles: _*)
  val hash = file.hash(files)

  def assertConfig(assetInfo: AssetsInfo, files: List[File], deployer: Option[Deployer] = None)(c: AssetsLoaderConfig, expectedFiles: String*): org.specs2.execute.Result = {
    import com.ee.utils.file.{hash => hashFn, fileToString}
    val processor = new SimpleFileProcessor(assetInfo, c, new File("test/mock-target"), Loader.ScriptTemplate, ".js", loader.minifyJs, hashFn(_, fileToString), deployer)
    val out = processor.process("test", files)
    out.length === expectedFiles.length
    assertSrc(out, expectedFiles: _*)
  }

  def assertSrc(scripts: List[String], sources: String*): MatchResult[Any] = {
    val xml = scala.xml.XML.loadString("<head>" + scripts.mkString("\n") + "</head>")
    val assertions = (xml \\ "script").toList.zipWithIndex.map {
      t: (Node, Int) =>
        val (node,index) = t
        (node \ "@src").text === sources(index)
    }

    (assertions).filterNot {
      r => r match {
        case MatchSuccess(_, _, _) => true
        case _ => false
      }
    }.length === 0
  }

  def path(s: String*) = s.mkString("/")


  val assertNoDeploy = assertConfig(assetInfo, files) _
  val assertDeploy = assertConfig(assetInfo, files, Some(new NullDeployer)) _

  def run(leadPath: String, deploy: Boolean, fn: (AssetsLoaderConfig, String *) => org.specs2.execute.Result) = {
    "when working with a folder and a path" in {


      "work" in {
        fn(
          AssetsLoaderConfig(false, false, false, deploy),
          leadPath + "/" + relativeRoot + "/folder_one/one.js",
          leadPath + "/" + relativeRoot + "/root_one.js"
        )
      }

      "concat" in {
        fn(
          AssetsLoaderConfig(concatenate = true, false, false, deploy),
          leadPath + "/test-" + hash + ".js")
      }


      "minify - but don't concat" in {
        fn(
          AssetsLoaderConfig(concatenate = false, minify = true, gzip = false, deploy),
          leadPath + "/" + relativeRoot + "/folder_one/one.min.js",
          leadPath + "/" + relativeRoot + "/root_one.min.js"
        )
      }


      "gzip - but don't concat" in {
        fn(
          AssetsLoaderConfig(concatenate = false, minify = false, gzip = true, deploy),
          leadPath + "/" + relativeRoot + "/folder_one/one.gz.js",
          leadPath + "/" + relativeRoot + "/root_one.gz.js"
        )
      }


      "minify + gzip - but don't concat" in {
        fn(
          AssetsLoaderConfig(concatenate = false, minify = true, gzip = true, deploy),
          leadPath + "/" + relativeRoot + "/folder_one/one.min.gz.js",
          leadPath + "/" + relativeRoot + "/root_one.min.gz.js"
        )
      }


      "minify" in {
        fn(
          AssetsLoaderConfig(concatenate = true, minify = true, false, deploy),
          leadPath + "/test-" + hash + ".min.js")
      }

      "gzip" in {
        fn(
          AssetsLoaderConfig(concatenate = true, minify = true, gzip = true, deploy),
          leadPath + "/test-" + hash + ".min.gz.js")
      }

    }
  }


  "with no deployer" in {
    run("/webpath", false, assertNoDeploy)
  }


  "with a deployer" in {
    run("/deployed", true, assertDeploy)
  }

}


trait MockTargetFolder extends Specification {

  import java.io.{File}



  def targetRoot: String

  def targetFolder: String

  def contentsPath: String

  override def map(fs: => Fragments) = Step(before) ^ fs ^ Step(after)

  def before {
    //Note - using the full syntax here so we don't conflict with specs2
    println("copying over to test target dir.." + targetFolder)
    val dir = new File(targetFolder)
    if (!dir.exists()) {
      dir.mkdir()
    }

    util.copyTree(contentsPath, targetFolder)
  }

  def after {
    println("removing target dir.")
    util.deleteTree(targetRoot)
  }

}

