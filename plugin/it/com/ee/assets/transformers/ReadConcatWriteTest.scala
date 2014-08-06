package com.ee.assets.transformers

import com.ee.log.Logger
import org.specs2.mutable.Specification

class ReadConcatWriteTest extends Specification with BaseIntegration{

  val logger = Logger("test")

  val fileOut = "concatenated.txt"

  "Read, Concat, Write" should {

    "work" in {

      val read = ElementReader(readFn("it"))
      val concat = Concatenator(new PathNamer {

        override def name[A](elements: Seq[Element[A]], hashCode: Int): String = fileOut
      })

      val write = ElementWriter(writeFn("target"))

      val elements = Seq(
        PathElement(makePath(pkg, "files", "one.txt")),
        PathElement(makePath(pkg, "files", "two.txt"))
      )

      (read andThen concat andThen write)(elements)

      readFn("target")(fileOut).map {
         e =>
          e.contents === "one\ntwo"
          success
      }.getOrElse(failure("can't find file"))

    }
  }
}
