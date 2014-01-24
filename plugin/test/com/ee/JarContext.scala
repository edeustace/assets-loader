package com.ee

import com.ee.utils.file._
import java.io.File
import org.specs2.mutable.BeforeAfter

class JarContext(val jarContentsPath : String, val outputDirPath:String) extends BeforeAfter {

  lazy val jarDir = new File(jarContentsPath)

  lazy val allFiles = recursiveListFiles(jarDir).map( relativePath(_, jarDir) )

  lazy val jarPath = s"target/${jarDir.getName}.jar"

  lazy val destDir = new File(outputDirPath)

  def before: Any = {
    import scala.sys.process._
    val rmCmd = s"rm -rf ${new File(outputDirPath).getAbsolutePath}"
    val rmJarCmd = s"rm -rf $jarPath"
    val cmd = s"jar cvfM $jarPath -C ${jarDir.getAbsolutePath}/ ."
    println(rmCmd.!!)
    println(rmJarCmd.!!)
    println(cmd.!!)

    val created = outputDirPath.split("/").foldLeft( new File("") ){ (root:File, name: String)  =>
      val f = new File(s"${root.getAbsolutePath}/$name")
      f.mkdir()
      f
    }
    println(s" created: ${created.getAbsolutePath}")
  }

  def after: Any = {

  }
}
