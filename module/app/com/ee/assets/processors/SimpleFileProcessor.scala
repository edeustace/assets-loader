package com.ee.assets.processors

import com.ee.assets.AssetProcessor
import com.ee.assets.models.AssetsLoaderConfig
import com.ee.assets.models.AssetsInfo
import play.api.Play
import play.api.Play.current
import java.io.{FileWriter, File}
import com.ee.utils.string._
import com.ee.utils.file._
import com.ee.js.JavascriptCompiler

class SimpleFileProcessor(info : AssetsInfo, config : AssetsLoaderConfig, targetFolder: String) extends AssetProcessor {

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""
  def scriptTag(url:String) : String = interpolate(ScriptTemplate, ("src", url))

  private def concatFiles(files:List[File], destination: String) {
    import com.ee.js.JavascriptCompiler

    val contents = files 
      .filter(isJs)
      .map(f => readContents(f)).mkString("\n")

    val out = if(config.minify) JavascriptCompiler.minify(contents, None) else contents
    println(">> write to: " + destination)
    writeToFile(destination, out)
  }

  private def minifyFile(file:File, destination:String) {
    val contents = readContents(file) 
    val out = JavascriptCompiler.minify(contents, None)
    println("minifyFile: " + destination)
    writeToFile(destination, out)
  }

  private def gzipFile(file:File, destination:String){
    val contents = readContents(file)
    println("gzipping: " + destination)
    com.ee.utils.gzip.gzip(contents, destination)  
  }

  private def tidyFolders(s:String) : String = s.replace("/./", "/").replace("//", "/")

  def process(path:String) : String = {

    def concat(files:List[File]) : Option[List[File]] = if(config.concatenate){

      val filesHash = hash(files)
      val newJsFile = path + "-" + filesHash +".js"
      val destination = targetFolder + info.filePath + "/" + newJsFile 
      
      if( !(new File(destination).exists )){
        println("creating new file: " + destination)
        concatFiles(files, destination)
      } 
      else {
        println("file already exists: " + new File(destination).getAbsolutePath)
      }
      Some(List(new File(destination)))
    } else {
      Some(files)
    }


    def minify(files:List[File]) : Option[List[File]]= if(config.minify){

      val destinationRoot = new File(targetFolder + info.filePath).getAbsolutePath

      val minified = files.filter(isJs).map{ f => 

        val absolutePath = tidyFolders(f.getAbsolutePath)
        val relativePath = absolutePath.replace(destinationRoot, "").replace(f.getName, "")
        val minifiedName = com.ee.utils.file.basename(f) + ".min.js"
        val destination = targetFolder + info.filePath + "/" + relativePath + "/" + minifiedName

        if( !(new File(destination).exists)){
          println("minimizing: " + destination)
          minifyFile(f, destination)
        } 
        new File(destination)
      }
      Some(minified) 
    } else {
      Some(files)
    }    

    def gzip(files:List[File]) : Option[List[File]] = if(config.gzip){

      val destinationRoot = new File(targetFolder + info.filePath).getAbsolutePath

      val processed = files.filter(isJs).map{ f => 

        println("processing: " + f.getName)
        val absolutePath = tidyFolders(f.getAbsolutePath)
        val relativePath = absolutePath.replace(destinationRoot, "").replace(f.getName, "")
        val processedName = com.ee.utils.file.basename(f) + ".gz.js"
        val destination = targetFolder + info.filePath + "/" + relativePath + "/" + processedName

        if( !(new File(destination).exists)){
          println("gzipping: " + destination)
          gzipFile(f, destination)
        } 
        new File(destination)
      }
      Some(processed) 
    } else {
      Some(files)
    }    

    println("path: " + path)
    println("info: " + info)
    val file = Play.getFile( info.filePath + "/" + path)

    def processFileList(files:List[File]) : String = {

      val processed : Option[List[File]] = for{
        concatenated <- concat(files)
        minified <- minify(concatenated)
        gzipped <- gzip(minified)
      } yield {
        gzipped
      }

      processed.map{ files => 
        files.filter(_.getName.endsWith(".js")).map{ f:File =>
            val rootPath : String = file.getAbsolutePath.replace(path, "")
            val filePath = f.getAbsolutePath.replace(rootPath, "") 
            scriptTag(info.webPath+ "/" + filePath)
        }.mkString("\n")
      }.getOrElse("")


      /*if(config.concatenate){
        val newJsFile = concatenate(path, files, info)
        scriptTag(info.urlRoot + "/" + newJsFile)
      } else {

        allFiles.filter(isJs).map{ f : File => 
          val rootPath : String = file.getAbsolutePath.replace(path, "")
          val filePath = f.getAbsolutePath.replace(rootPath, "") 
          scriptTag(info.urlRoot + "/" + filePath)
          }.mkString("\n")
      }*/
    }

    /*def processSingleFile(file:File) : String = if(path.endsWith(".js")) {
      scriptTag(info.urlRoot + "/" + path)
    } else {
      "<!-- can't find file: " + path + " -->"
    }*/

    println("file: " + file.getAbsolutePath + " exists: " + file.exists)
    val fileList : List[File] = recursiveListFiles(file)
    processFileList(fileList)
  }

  private def isJs(f:File) : Boolean = {
    f.isFile && f.getName.endsWith(".js") && !f.getName.endsWith(".gz.js")
  }

  private def concatenate( path : String, files:List[File], info : AssetsInfo) = {



      val filesHash : String = hash(files)

      val newJsFile = path + "-" + filesHash +".js"
      val destination = targetFolder + info.filePath + "/" + newJsFile 
      
      if( !(new File(destination).exists )){
        println("creating new file: " + destination)
        concatFiles(files, destination)
      } 
      else {
        println("file already exists: " + new File(destination).getAbsolutePath)
      }
      newJsFile
    }


  /** Create a hash from the file list so we can uid it against other files
   */
  private def hash(files: List[File]) : String = {

    val timestamps : String = files
      .sortWith((a:File,b:File) => a.getName < b.getName)
      .map( f => f.getAbsolutePath() + "_" + f.lastModified())
      .mkString("__")

    timestamps.hashCode().toString
  }
 


}