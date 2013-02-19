package com.ee.assets.processors

import org.specs2.mutable.Specification
import java.io.File
import scala.xml.{XML, Node, Elem}

import com.ee.assets.models._
import org.specs2.matcher.{MatchSuccess, MatchResult}

class SimpleFileProcessorTest extends Specification {

  helpers.PlaySingleton.start

  "File Processor" should {


    def assertSrc(xml: Elem, sources: String*) : MatchResult[Any] = {
      println("assertSrc")
      println(xml)
      val lengthAssertion = (xml \\ "script").length === sources.length
      val assertions = (xml \\ "script").toList.zipWithIndex.map {
        t: (Node, Int) =>
          (t._1 \ "@src").text === sources(t._2)
      }

      (assertions :+ lengthAssertion).filterNot{ r =>  r match {
        case MatchSuccess(_,_,_) => true
        case _ => false
      }
      }.length === 0
    }
    
    "work" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testOne")
      val config = AssetsLoaderConfig(false, false, false)
      val processor = new SimpleFileProcessor(assetInfo, config, "")

      val out = processor.process("files_one")
      println(out)
      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2

       assertSrc(xml, "/webpath/files_one/one.js", "/webpath/files_one/one/one_one.js")
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
      assertSrc(xml, "/webpath/" + generatedFile.getName )
    }

    "minify" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testThree")
      val config = AssetsLoaderConfig(false, true, false)
      val processor = new SimpleFileProcessor(assetInfo, config, "")
      val out = processor.process("files_one")

      println("minify out: " + out)
      val files = com.ee.utils.file.recursiveListFiles(new File(root + "/testThree"))
      files.filter(_.getName.endsWith("min.js")).map(_.delete)

      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2
      (xml \\ "script").filterNot {
        n: Node =>
          (n \ "@src").text.contains("min.js")
      }.length === 0

     assertSrc(xml, "/webpath/files_one/one.min.js", "/webpath/files_one/one/one_one.min.js" )
    }
    

    "gzip" in {
      val root = "test/mockFiles/com/ee/assets/processors"
      val assetInfo = new AssetsInfo("/webpath", root + "/testFour")
      val config = AssetsLoaderConfig(false, false, true)
      val processor = new SimpleFileProcessor(assetInfo, config, "")
      val out = processor.process("files_one")

      println("received: " + out)
      val files = com.ee.utils.file.recursiveListFiles(new File(root + "/testFour"))
      files.filter(_.getName.endsWith("gz.js")).map(_.delete)

      val xml = scala.xml.XML.loadString("<head>" + out + "</head>")
      (xml \\ "script").length === 2
      assertSrc(xml, "/webpath/files_one/one.gz.js", "/webpath/files_one/one/one_one.gz.js" )
    }
    

  }
}