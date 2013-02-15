package com.ee.assets.processors

import org.specs2.mutable.Specification
import java.io.File
import scala.xml.{XML, Node}

import com.ee.assets.models._

class SimpleFileProcessorTest extends Specification {

  helpers.PlaySingleton.start

  "File Processor" should {

    "work" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testOne")
      val config = AssetsLoaderConfig(false, false, false)
      val processor = new SimpleFileProcessor(assetInfo, config, "com/ee/assets/processors/testOne")

      val out = processor.process("files_one")
      println(out)
      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2
    }

    "concat" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testTwo")
      val config = AssetsLoaderConfig(true, false, false)
      val processor = new SimpleFileProcessor(assetInfo, config, "")

      val out = processor.process("files_one")
      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 1

      val generatedFile = new File(root + "/testTwo").listFiles
        .toList
        .find(_.getName.endsWith(".js")).get
      println("found generated file: " + generatedFile.getName)
      val out_two = processor.process("files_one")
      out_two === out

      generatedFile.delete
    }

    "minify" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testThree")
      val config = AssetsLoaderConfig(false, true, false)
      val processor = new SimpleFileProcessor(assetInfo, config, "")
      val out = processor.process("files_one")

      val files = com.ee.utils.file.recursiveListFiles(new File(root + "/testThree"))
      files.filter(_.getName.endsWith("min.js")).map(_.delete)

      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2
      (xml \\ "script").filterNot {
        n: Node =>
          (n \ "@src").text.contains("min.js")
      }.length === 0
    }


    "gzip" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testFour")
      val config = AssetsLoaderConfig(false, false, true)
      val processor = new SimpleFileProcessor(assetInfo, config, "")
      val out = processor.process("files_one")

      println("received: " + out)
      val files = com.ee.utils.file.recursiveListFiles(new File(root + "/testFour"))
      //files.filter(_.getName.endsWith("gz.js")).map(_.delete)

      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2
    }
  }
}