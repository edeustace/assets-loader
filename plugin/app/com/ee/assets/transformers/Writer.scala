package com.ee.assets.transformers

import com.ee.log.Logger

class Writer(writeFn: (String, String) => Unit) extends Transformer {

  lazy val logger = Logger("writer")

  override def run(elements: Seq[Element]): Seq[Element] = {


    elements.map {
      e =>

        e.contents.map {
          c =>
            writeFn(e.path, c)
        }.getOrElse {
          logger.warn(s"Nothing to write for: ${e.path}")
        }

        e.copy(contents = None)
    }
  }
}
