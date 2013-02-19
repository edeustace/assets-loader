package com.ee.utils

import java.io.File
import java.io.FileWriter

package object file {

  def writeToFile(path: String, contents: String): File = {
    val fw = new FileWriter(path)
    fw.write(contents)
    fw.close()
    val newFile = new File(path)
    if(newFile.exists) new File(path) else throw new RuntimeException("Couldn't write the file to: " + path)
  }

  def readContents(f: File): String = {

    if( f.exists ){
      val source = scala.io.Source.fromFile(f)
      val lines = source.mkString
      //println("utils.file.readContents: found file: " + f.getName)
      //println(lines)
      source.close()
      lines
    }
    else {
      //println("utils.file.readContents: file doesn't exist: " + f.getName)
      ""
    }
  }

  def recursiveListFiles(f: File): List[File] = f match {
    case doesntExist: File if !f.exists() => List()
    case file: File if f.isFile => List(file)
    case directory: File if f.isDirectory => {
      val files = directory.listFiles.toList
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
    case _ => List()
  }

  def suffix(f:File) : String = if(f.getName.contains(".")){
    f.getName.split("\\.").last
  }
  else {
    f.getName
  }

  def basename(f:File) : String = if(f.getName.contains(".")){
    f.getName.split("\\.").dropRight(1).mkString(".")
  }
  else {
    f.getName
  }

}