package com.ee.assets.transformers

import com.ee.log.Logger

trait PathNamer {
  def name[A](elements: Seq[Element[A]]): String
}

class CommonRootNamer(prefix: String, suffix: String) extends PathNamer {

  lazy val logger = Logger("path-namer")

  override def name[A](elements: Seq[Element[A]]): String = {
    import com.ee.utils.file.commonRootFolder

    logger.trace(s"get common root for: ${elements.map(_.path)}")
    val root = commonRootFolder(elements.map(_.path): _*)
    logger.trace(s"commonRootFolder: $root")
    val hash = elements.map(_.path).mkString(",").hashCode
    s"${root}/$prefix-$hash.$suffix"
  }
}
