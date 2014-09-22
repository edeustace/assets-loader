package com.ee.assets.transformers

import java.io.File
import org.apache.commons.io.FileUtils
import org.specs2.mutable.{Specification, Before}


class cleanGenerated(generatedPath: String) extends Before {
  override def before: Any = {

    val f = new File(generatedPath)

    if (f.exists && f.isDirectory) {
      println(s"deleting: $generatedPath")
      grizzled.file.util.deleteTree(new File(generatedPath))
    }
  }
}

trait BaseIntegration extends Specification {

  sequential

  def pkg = {
    makePath(this.getClass.getPackage.getName.split("\\."): _*)
  }

  def makePath(parts: String*) = parts.mkString(File.separator)

  def readFn(root: String)(path: String): Option[Element[String]] = {

    val readPath = makePath(root, path)

    println("readFn: " + readPath)
    val f = new File(readPath)

    if (f.exists) {
      Some(ContentElement(path, FileUtils.readFileToString(f), None))
    } else {
      println(s"file: $f doesn't exist")
      None
    }
  }

  def resolveFileFn(root:String)(path:String) : File = new File(s"$root${File.separator}$path")

  def writeFn(root: String)(e: Element[String]): String = {

    import com.ee.utils.file.writeToFile

    val path = makePath(root, e.path)
    println(s"[writeFn] $path")
    writeToFile(makePath(root, e.path), e.contents)
    e.path
  }

}
