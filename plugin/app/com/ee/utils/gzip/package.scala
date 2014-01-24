package com.ee.utils

import java.util.zip._
import java.io._

package object gzip {

  def gzip(s:String,filePath:String) : File = {
   
    val fileOut = new File(filePath)
    fileOut.delete
    val outputStrem = new FileOutputStream(fileOut) 
    val zip = new GZIPOutputStream(outputStrem)
    val writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"))
    val data : Array[String] = s.split("\n")

    for( line <- data)
    {
      writer.append(line)
      writer.newLine()
    }

    writer.close()
    new File(filePath)
  }
}