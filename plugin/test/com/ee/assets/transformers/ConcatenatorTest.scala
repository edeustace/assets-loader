package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ConcatenatorTest extends Specification {

  "Concatenator" should {
    "concat" in {
      val elements = Seq(ContentElement("a.txt", "a", None), ContentElement("b.txt", "b", None))

      val namer = new PathNamer {
        override def name[A](elements: Seq[Element[A]]): String = "concatenated.txt"
      }

      new Concatenator(namer).run(elements) === Seq(ContentElement("concatenated.txt", "a\nb\n", None))
    }
  }
}


