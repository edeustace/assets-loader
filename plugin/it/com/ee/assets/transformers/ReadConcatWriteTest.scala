package com.ee.assets.transformers

import org.specs2.mutable.Specification
import com.ee.log.Logger

class ReadConcatWriteTest extends Specification with BaseIntegration{

  val logger = Logger("test")

  val fileOut = "concatenated.txt"

  "Read, Concat, Write" should {

    "work" in {

      val read = new ElementReader(readFn("it"))
      val concat = new Concatenator(new PathNamer {
        override def name(elements: Seq[Element]): String = fileOut
      })
      val write = new ElementWriter(writeFn("target"))

      val sequence = new TransformationSequence(read, concat, write)

      val elements = Seq(
        Element(makePath(pkg, "files", "one.txt")),
        Element(makePath(pkg, "files", "two.txt"))
      )
      sequence.run(elements)

      readFn("target")(fileOut).map {
        c =>
          c === "one\ntwo"
          success
      }.getOrElse(failure("can't find file"))

    }
  }
}
