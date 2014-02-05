package com.ee.assets.transformers

import org.specs2.mutable.Specification

class TransformationSequenceTest extends Specification {


  "sequence" should {

    "run" in {

      val transformers : Seq[Transformer[String,String]] = Seq(
        new Transformer[String,String] {
          override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {
            elements.map(e => Element(e.path, Some("a")))
          }
        }
      )

      val transformation = new TransformationSequence(transformers : _*)
      val elements = Seq(Element[String]("a.txt"))
      val out = transformation.run(elements)
      out === Seq(Element("a.txt", Some("a")))
    }
  }
}
