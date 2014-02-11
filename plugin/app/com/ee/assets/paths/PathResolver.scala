package com.ee.assets.paths

import com.ee.log.Logger
import java.io.File
import java.net.{URLDecoder, URL}
import java.util.jar.JarFile

object PathResolver {

  lazy val logger = Logger("paths")

  def resolve(path: String, url: URL): Seq[String] = {

    val paths = url.getProtocol match {
      case "jar" => jarPaths(url)
      case "file" => filePaths(path, url)
    }

    paths.sortWith {
      (a, b) =>

        val aSlashCount = a.count(_ == '/')
        val bSlashCount = b.count(_ == '/')

        if (aSlashCount == bSlashCount) {
          a < b
        } else {
          aSlashCount < bSlashCount
        }
    }

  }

  private def jarPaths(url: URL): Seq[String] = {
    logger.trace(s"[jarPaths] url: $url")
    val jarPath = url.getPath().substring(5, url.getPath().indexOf("!"))
    require(url.getFile.contains("!/"))
    val filePath = url.getFile.split("!/")(1)
    val jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
    def folders(p: String) = p.endsWith("/")
    val noFolders = (folders(_: String) == false)
    def startsWith(p: String) = {
      p.startsWith(filePath)
    }
    def finalFilter(p: String) = startsWith(p) && noFolders(p)
    com.ee.utils.jar.listChildrenInJar(jarFile, finalFilter)
  }

  private def filePaths(path: String, url: URL): Seq[String] = {
    logger.trace(s"[listAllChildrenFromFolder] : $url")
    val root = new File(url.getPath)
    import com.ee.utils.file.distinctFiles
    val allFiles = distinctFiles(root)
    def isFile(f: File) = f.isFile
    def finalFn(f: File) = isFile(f)

    def backSlashToForwardSlash(p: String) = p.replaceAll("\\\\", "/")

    def relativePath(p: String) = {
      logger.trace(s"file path: $p")
      val splitIndex = p.indexOf(path)
      require(splitIndex != -1, s"$p doesn't contain $path")
      p.substring(splitIndex)
    }

    allFiles
      .filter(finalFn)
      .map(_.getPath)
      .map(backSlashToForwardSlash)
      .map(relativePath)
  }
}
