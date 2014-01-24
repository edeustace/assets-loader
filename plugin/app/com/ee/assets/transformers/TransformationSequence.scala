package com.ee.assets.transformers

import com.ee.log.Logger

class TransformationSequence(transformers:Transformer*) extends Transformer{

  lazy val logger = Logger("Sequence")
  override def run(elements:Seq[Element]): Seq[Element] = {

    logger.trace(s"run: $elements")
    transformers.tail.foldLeft(transformers.head.run(elements)){ (elements, t) =>
      logger.trace(s"transformer: $t")
      t.run(elements)
    }
  }
}
