package com.ee.utils

import java.io.{FileOutputStream, InputStream, File}
import java.util.jar.{JarEntry, JarFile}

package object jar {


  def extractJar(jarFile: File, destDir: File, filter: String => Boolean): File = {
    import collection.JavaConversions.enumerationAsScalaIterator

    require(destDir.exists(), s"[extractJar] The destination folder doesn't exist")

    val jar: JarFile = new JarFile(jarFile)
    val enum: Iterator[JarEntry] = jar.entries()

    def write(e: JarEntry) = {
      val f: File = new File(destDir.getAbsolutePath + java.io.File.separator + e.getName())

      if (e.isDirectory()) {
        f.mkdir()
      } else {
        val is: InputStream = jar.getInputStream(e)
        val os: FileOutputStream = new java.io.FileOutputStream(f)
        while (is.available() > 0) {
          os.write(is.read())
        }
        os.close()
        is.close()
      }
    }

    do {
      val entry = enum.next()

      if (filter(entry.getName)) {
        write(entry)
      }
    } while (enum.hasNext)

    destDir
  }

  def listChildrenInJar(jar: JarFile, filter: String => Boolean): Seq[String] = {
    import collection.JavaConversions.enumerationAsScalaIterator

    val entries: Iterator[JarEntry] = jar.entries()
    val out: Seq[String] = entries.foldLeft[Seq[String]](Seq()) {
      (acc, entry) =>
        if (filter(entry.getName)) {
          acc :+ entry.getName
        } else {
          acc
        }
    }
    out
  }

}
