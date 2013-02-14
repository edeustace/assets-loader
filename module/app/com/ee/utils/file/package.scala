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
    val source = scala.io.Source.fromFile(f)
    val lines = source.mkString
    source.close()
    lines
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

}