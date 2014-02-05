package com.ee.assets.transformers

import com.ee.log.Logger

class Concatenator(pathNamer: PathNamer, separator: String = "\n") extends Transformer[String,String] {

  lazy val logger = Logger("transformer.concatenator")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    logger.trace( s"run: ${elements.map(_.path).mkString(",")}")

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
