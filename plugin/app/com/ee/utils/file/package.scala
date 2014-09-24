package com.ee.utils

import _root_.com.ee.log.Logger
import java.io.File
import java.io.FileWriter

import org.apache.commons.io.FileUtils

package object file {

  lazy val logger = Logger("file")

  def commonRootFolder(paths: String*): String = {

    def buildCommon(a: Seq[String], b: Seq[String]) = {
      val intersection = a.intersect(b)
      if (a.startsWith(intersection) && b.startsWith(intersection)) {
        intersection
      } else {
        Seq.empty
      }
    }

    val prepped : Seq[Seq[String]] = paths.toSeq.map(_.split("/").toSeq)

    prepped match {
      case Nil => ""
      case Seq(head) =>  head.dropRight(1).mkString("/")
      case Seq(head, xs @ _*) => {
        val out: Seq[String] = xs.foldLeft(head) {
          (guess, current) =>
            buildCommon(guess, current)
        }
        out.mkString("/")
      }
    }
  }

  def writeToFile(path: String, contents: String, mkDir: Boolean = true): File = {

    if (mkDir) {
      new File(path).getParentFile().mkdirs
    }

    val fw = new FileWriter(path)
    fw.write(contents)
    fw.close()
    val newFile = new File(path)
    if (newFile.exists) new File(path) else throw new RuntimeException("Couldn't write the file to: " + path)
  }

  def readContents(f: File): String = {

    logger.debug("file.readContents: " + f.getName)
    if (f.exists) {
      FileUtils.readFileToString(f, "UTF-8")
    }
    else {
      ""
    }
  }

  def recursiveListFiles(f: File): List[File] = f match {
    case doesntExist: File if !f.exists() => {
      logger.warn("file doesn't exist: " + f.getName)
      List()
    }
    case file: File if f.isFile => List(file)
    case directory: File if f.isDirectory => {
      val files = directory.listFiles.toList
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
    case _ => List()
  }

  def distinctFiles(files: File*): List[File] = {
    val withChildren = files.toList.map(recursiveListFiles)
    withChildren.flatten.distinct
  }

  def relativePath(child: File, parent: File): String = {
    val childFullPath = child.getCanonicalPath
    val parentFullPath = parent.getCanonicalPath

    if (childFullPath.startsWith(parentFullPath)) {
      childFullPath.replace(parentFullPath, "")
    } else {
      throw new RuntimeException("Error getting relative path the child isn't actually a child: " + childFullPath + " parent: " + parentFullPath)
    }
  }


  def typeFilter(suffix: String, files: List[File]): List[File] = files.filter(_.getName.endsWith(suffix))

  def nameAndSuffix(f: File): (String, String) = nameAndSuffix(f.getName)

  def nameAndSuffix(s: String): (String, String) = {
    if (s.contains(".")) {
      val lastIndex = s.lastIndexOf(".")
      if (lastIndex == 0) {
        (s, "")
      } else {
        val name = s.substring(0, lastIndex)
        val suffix = s.substring(lastIndex + 1, s.length)
        (name, suffix)
      }
    } else {
      (s, "")
    }
  }

  def fileToString(f: File): String = f.getAbsolutePath + "_" + f.lastModified()

  /** Create a hash from the file list using the name and dateModified.
    */
  def hash(files: List[File],
           fileToString: (File => String) = fileToString): String = {
    val timestamps: String = files
      .sortWith((a: File, b: File) => a.getName < b.getName)
      .map(fileToString)
      .mkString("__")
    timestamps.hashCode().toString
  }

}