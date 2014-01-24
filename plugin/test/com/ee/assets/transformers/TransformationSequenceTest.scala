package com.ee.assets.transformers

import org.specs2.mutable.Specification

class TransformationSequenceTest extends Specification {


  "sequence" should {

    "run" in {

      val transformers = Seq(
        new Transformer {
          override def run(elements: Seq[Element]): Seq[Element] = {
            elements.map(e => Element(e.path, Some("a")))
          }
        }
      )

      val transformation = new TransformationSequence(transformers : _*)
      val elements = Seq(Element("a.txt"))
      val out = transformation.run(elements)
      out === Seq(Element("a.txt", Some("a")))
    }
  }
}
