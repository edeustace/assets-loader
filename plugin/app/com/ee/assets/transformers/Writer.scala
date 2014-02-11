package com.ee.assets.transformers

import com.ee.log.Logger

class Writer(writeFn: (String, String) => Unit) extends Transformer[String, Unit] {

  lazy val logger = Logger("writer")

  override def run(elements: Seq[Element[String]]): Seq[PathElement] = {
    elements.map {
      e =>
        writeFn(e.path, e.contents)
        PathElement(e.path)
    }
  }
}
