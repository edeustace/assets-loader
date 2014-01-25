package com.ee

import com.ee.utils.file._
import grizzled.file.util
import java.io.File
import org.specs2.mutable.BeforeAfter

class JarContext(val jarContentsPath : String, val outputDirPath:String) extends BeforeAfter {

  lazy val jarDir = new File(jarContentsPath)

  lazy val allFiles = recursiveListFiles(jarDir).map( relativePath(_, jarDir) )

  lazy val jarPath = s"target/${jarDir.getName}.jar"

  lazy val destDir = new File(outputDirPath)

  def before: Any = {

    import scala.sys.process._


    deleteIfExists(outputDirPath)
    deleteIfExists(jarPath)

    val cmd = s"jar cvfM $jarPath -C ${jarDir.getAbsolutePath}/ ."
    println(cmd.!!)
    new File(outputDirPath).mkdirs()
  }

  private def deleteIfExists(p:String) = {
    val f = new File(p)
    if(f.exists){
      if(f.isDirectory){
        util.deleteTree(f)
      } else {
        f.delete
      }
    }
  }

  def after: Any = {

  }
}
