package com.ee.assets.transformers

import com.ee.log.Logger

object Concatenator{
  def apply(pathNamer : PathNamer, separator : String = "\n") = new Concatenator(pathNamer, separator).run _
}

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

    val contents = builder.toString
    val concatName = pathNamer.name(elements, contents.hashCode)
    val lm = elements
      .map(_.lastModified)
      .flatten
      .sorted
      .reverse
      .headOption

    logger.trace(s"concatenated name: $concatName")

    Seq(ContentElement(concatName, contents, lm))
  }

}
