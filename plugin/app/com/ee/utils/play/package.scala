package com.ee.utils

import java.io._

package object play {

  val Separator = sys.env.get("file.separator").getOrElse("/")

  def classesFolder() : File = {
    val target : File = new File("target")
    target.listFiles.toList.find(_.getName.startsWith("scala-")) match {
      case Some(scalaFolder) => {
        val path = List(target.getName, scalaFolder.getName, "classes").mkString(Separator)
        val file : File =  new File(path)
        if(file.exists) file else throw new RuntimeException("can't find classes folder in target") 
      }
      case _ => throw new RuntimeException("can't find scala-${version} folder") 
    }
  }
}