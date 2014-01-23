package com.ee.assets.transformers

import com.ee.log.Logger

class Concatenator(pathNamer: PathNamer, separator: String = "\n") extends Transformer {

  lazy val logger = Logger("transformer.concatenator")

  override def run(elements: Seq[Element]): Seq[Element] = {

    logger.trace( s"run: $elements")

    Seq.empty

    val builder = new StringBuilder("")

    elements.foreach {
      (e) =>
        e.contents.map(builder.append)
        builder.append(separator)
    }
    val concatName = pathNamer.name(elements)
    logger.debug(s"name: $concatName")
    Seq(Element(concatName, Some(builder.toString())))
  }

}
