package com.ee.assets.transformers

import com.ee.log.Logger

class Concatenator(pathNamer: PathNamer, separator: String = "\n") extends Transformer[String,String] {

  lazy val logger = Logger("transformer.concatenator")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    logger.trace( s"run: ${elements.map(_.path).mkString(",")}")

    val builder = new StringBuilder("")

    elements.foreach {
      (e) =>
        builder.append(e.contents)
        builder.append(separator)
    }

    val concatName = pathNamer.name(elements)
    logger.debug(s"name: $concatName")
    val lm = elements
      .map(_.lastModified)
      .flatten
      .sorted
      .reverse
      .headOption

    Seq(ContentElement(concatName, builder.toString, lm))
  }

}
