package com.ee.assets

import play.api.Play
import play.api.Play.current
import java.io.{FileWriter, File}
import play.api.templates.Html
import com.ee.utils.string._
import com.ee.utils.file._

object Loader{
  
  case class AssetsInfo(urlRoot:String, filePath:String)

  lazy val assetsInfo : Option[AssetsInfo] = getAssetsInfo

  case class AssetsLoaderConfig(concatenate:Boolean, minify: Boolean)

  lazy val config : AssetsLoaderConfig = {

     val modeKey = Play.mode match {
       case play.api.Mode.Dev => "dev"
       case play.api.Mode.Test => "test"
       case play.api.Mode.Prod => "prod"
     }

     println("!! > mode: " + modeKey)

     def bool(property:String, default : Boolean = false) : Boolean = {
      val maybeBoolean = current.configuration
        .getBoolean("assetsLoader." + modeKey + "." + property)
      println("property: " + property + ": " + maybeBoolean)
      maybeBoolean.getOrElse(default)
     }

     val concatenate : Boolean = bool("concatenate")
     val minify : Boolean = bool("minify")

     AssetsLoaderConfig(concatenate, minify)
  }

  /** Load in the assets controller info from the routes file
   */
  def getAssetsInfo : Option[AssetsInfo] = {
    Some(AssetsInfo("/assets", "/public"))

    /*val Regex = """.*GET.*?(/.*)/\*file[\s|\t]*com.ee.assets.controllers.LoaderAssets.at\(path="(.*?)",.*\)""".r
    val routes = Play.getFile("conf/routes") 
    val contents : List[String] = readContents(routes).split("\n").toList
    contents.find( _.contains("controllers.Assets.at")) match {
      case Some(line) => {
        println(line)
        val Regex(urlRoot, filePath) = line
        Some(AssetsInfo(urlRoot, filePath))
      }
      case None => None
    }*/
  }

  lazy val targetFolder : String = {
    val Regex = """version (.*)""".r
    val Regex(number) = util.Properties.versionString
    val cleaned = number.replace(".final", "")
    "target/scala-" + cleaned + "/classes/"
  }

  val AssetLoaderTemplate =
    """<!-- Asset Loader -->
      |    ${content}
      |<!-- End -->
    """.stripMargin

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""


  def scripts( paths : String*) : play.api.templates.Html = {

    println("Loader.scripts")
    println("config: " + config)
    println(util.Properties.versionString)

    val info = assetsInfo.getOrElse(throw new RuntimeException("can't find AssetsInfo"))

    def scriptTag(url:String) : String = interpolate(ScriptTemplate, ("src", url))

    def toScript(path:String) : String = {

      println("to script: " + path)

      val file = Play.getFile( info.filePath + "/" + path)
      println("file: " + file.getAbsolutePath + " exists: " + file.exists)
      if(file.isDirectory){
        val allFiles: List[File] = recursiveListFiles(file)

        println(allFiles)

        if(config.concatenate){
          val newJsFile = concatenate(path, allFiles, info)
          scriptTag(info.urlRoot + "/" + newJsFile)
        } else {

          allFiles.filter(isJs).map{ f : File => 
            val rootPath : String = file.getAbsolutePath.replace(path, "")
            val filePath = f.getAbsolutePath.replace(rootPath, "") 
            scriptTag(info.urlRoot + "/" + filePath)
            }.mkString("\n")
        }

      } else {
        if(path.endsWith(".js")){
            scriptTag(info.urlRoot + "/" + path)

        } else {
          "<!-- can't find file: " + path + " -->"
        }
      }
    }

    val scripts = paths.toList.map(toScript)
    val out = interpolate(AssetLoaderTemplate, ("content", scripts.mkString("\n")))
    Html(out)
  }

  private def isJs(f:File) : Boolean = f.isFile && f.getName.endsWith(".js")

  private def concatenate( path : String, files:List[File], info : AssetsInfo) = {

      def concatFiles(files:List[File], destination: String) {
        import com.ee.js.JavascriptCompiler

        val contents = files 
          .filter(isJs)
          .map(f => readContents(f)).mkString("\n")

        val out = if(config.minify) JavascriptCompiler.minify(contents, None) else contents

        println(">> write to: " + destination)
        writeToFile( destination, out)
      }


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