package com.ee.assets

import play.api.Play
import play.api.Play.current
import java.io.{FileWriter, File}
import play.api.templates.Html
import com.ee.utils._

object Loader{

  
  case class AssetsInfo(urlRoot:String, filePath:String)

  lazy val assetsInfo : Option[AssetsInfo] = getAssetsInfo

  /** Load in the assets controller info from the routes file
   */
  def getAssetsInfo : Option[AssetsInfo] = {
    val Regex = """.*GET.*?(/.*)/\*file[\s|\t]*controllers.Assets.at\(path="(.*?)",.*\)""".r
    val routes = Play.getFile("conf/routes") 
    val contents : List[String] = readContents(routes).split("\n").toList
    contents.find( _.contains("controllers.Assets.at")) match {
      case Some(line) => {
        println(line)
        val Regex(urlRoot, filePath) = line
        Some(AssetsInfo(urlRoot, filePath))
      }
      case None => None
    }
  }

  lazy val targetFolder : String = {
    val Regex = """version (.*)""".r
    val Regex(number) = util.Properties.versionString
    val cleaned = number.replace(".final", "")
    "target/scala-" + cleaned + "/classes/"
  }

  val AssetLoaderTemplate =
    """<!-- Asset Loader -->
      |${content}
      |<!-- End -->
    """.stripMargin

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""


  def scripts( paths : String*) : play.api.templates.Html = {

    println("assetsInfo")
    println(assetsInfo)

    println(util.Properties.versionString)

    val info = assetsInfo.getOrElse(AssetsInfo("/a", "/p"))

    def toScript(path:String) : String = {

      val file = Play.getFile( "." + info.filePath + "/" + path.replace(".js", ""))
      val allFiles: List[File] = recursiveListFiles(file)
      val allContents = allFiles
        .filter(f => f.isFile && f.getName.endsWith(".js"))
        .map(f => readContents(f)).mkString("\n")

      val outPath = targetFolder + info.filePath + "/" + path
      println(">> write to: " + outPath )
      writeToFile( outPath, allContents)
      string.interpolate(ScriptTemplate, ("src", info.urlRoot + "/" + path ))
    }

    val scripts = paths.toList.map(toScript)
    val out = string.interpolate(AssetLoaderTemplate, ("content", scripts.mkString("\n")))
    Html(out)
  }

  def writeToFile(path: String, contents: String): File = {
    val fw = new FileWriter(path)
    fw.write(contents)
    fw.close()
    new File(path)
  }

  def readContents(f: File): String = {
    val source = scala.io.Source.fromFile(f)
    val lines = source.mkString
    source.close()
    lines
  }

  def recursiveListFiles(f: File): List[File] = f match {
    case doesntExit: File if !f.exists() => List()
    case file: File if f.isFile => List(file)
    case directory: File if f.isDirectory => {
      val files = directory.listFiles.toList
      files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
    case _ => List()
  }

}