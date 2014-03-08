package com.ee.assets.paths

import com.ee.JarContext
import com.ee.log.Logger
import java.io.File
import java.net.URL
import org.specs2.mutable.Specification

class PathResolverTest extends Specification {

  lazy val logger = Logger("test")

  "path resolver" should {
    "work" in new JarContext("test/com/ee/assets/paths/testFiles", "target/pathsTmpJarFolder") {

      val resolver = PathResolver

      def toJarUrl(p: String) = {
        new URL(s"jar:file:$jarPath!/$p")
      }

      val jarList = resolver.resolve("jarOne", toJarUrl("jarOne"))

      def trimSlash(s: String) = if (s.startsWith("/")) s.substring(1) else s


      def toFileUrl(p:String) = {
        new URL(s"file:$jarContentsPath${File.separator}$p")
      }

      val fileList = resolver.resolve("jarOne", toFileUrl("jarOne"))

      logger.debug("file listing: " + fileList.mkString("\n"))
      logger.debug("jar listing: " + jarList.mkString("\n"))
      fileList === jarList
    }

  }
}
