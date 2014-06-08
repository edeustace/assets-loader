package com.ee.assets.transformers

import com.ee.log.Logger

trait PathNamer {
  def name[A](elements: Seq[Element[A]], hashCode:Int): String
}

class CommonRootNamer(prefix: String, suffix: String) extends PathNamer {

  lazy val logger = Logger("common-root-namer")

  override def name[A](elements: Seq[Element[A]], hashCode:Int): String = {
    import com.ee.utils.file.commonRootFolder

    logger.trace(s"get common root for: ${elements.map(_.path)}")
    val root = commonRootFolder(elements.map(_.path): _*)
    logger.trace(s"commonRootFolder: $root")
    val out = s"${root}/$prefix-$hashCode.$suffix"
    logger.trace(s"out: $out")
    out
  }
}
