package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ConcatenatorTest extends Specification {

  "Concatenator" should {
    "concat" in {
      val elements = Seq( Element("a.txt", Some("a")), Element("b.txt", Some("b")) )

      val namer = new PathNamer {
        override def name(elements: Seq[Element]): String = "concatenated.txt"
      }

      new Concatenator(namer).run(elements) === Seq(Element("concatenated.txt", Some("a\nb\n")))
    }
  }
}


