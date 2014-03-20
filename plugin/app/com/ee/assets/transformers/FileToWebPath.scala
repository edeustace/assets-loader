package com.ee.assets.transformers

import com.ee.assets.models.AssetsInfo
import com.ee.log.Logger

class FileToWebPath(info: AssetsInfo) extends Transformer[Unit,Unit] {

  lazy val logger = Logger("file-to-web")

  override def run(elements: Seq[Element[Unit]]): Seq[PathElement] = {
    elements.map(e => PathElement(toWebPath(e.path)))
  }

  private def toWebPath(p: String): String = {

    if (!p.contains(info.filePath)) {
      logger.warn(s"$p doesn't contain ${info.filePath} - so nothing to replace")
    }
    val out = p.replaceFirst(info.filePath, info.webPath)

    if (out.startsWith("/")) out else s"/$out"
  }
}
