package com.ee.utils

import _root_.play.api.Play
import _root_.play.api.Play.current
import com.ee.log.Logger
import java.io._

package object play {

  lazy val logger = Logger("play")

  val Separator = sys.env.get("file.separator").getOrElse("/")

  def generatedFolder: File = {
    val f = Play.getFile("target/.assets-loader-generated")

    if (!f.exists) {
      f.mkdirs()
    } else if (f.isFile) {
      f.delete()
      f.mkdir()
    }
    f
  }
}
