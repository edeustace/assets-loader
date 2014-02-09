package com.ee.assets.transformers

import java.io.File
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

  def readFn(root: String)(path: String): Option[String] = {

    val readPath = makePath(root, path)

    println("readFn: " + readPath)
    val f = new File(readPath)

    if (f.exists) {
      Some(scala.io.Source.fromFile(f).getLines.mkString("\n"))
    } else {
      println(s"file: $f doesn't exist")
      None
    }
  }

  def writeFn(root: String)(e: Element[String]): String = {

    import com.ee.utils.file.writeToFile

    e.contents.foreach {
      c =>
        val path = makePath(root, e.path)
        println(s"[writeFn] $path")
        writeToFile(makePath(root, e.path), c)
    }
    e.path
  }

}
