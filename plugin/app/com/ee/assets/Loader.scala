package com.ee.assets

import play.api.Play
import play.api.Play.current
import java.io.{FileWriter, File}
import play.api.templates.Html
import com.ee.utils.string._
import com.ee.utils.file._
import com.ee.assets.models._
import com.ee.assets.processors._


object Loader {

  private val processor: AssetProcessor = new SimpleFileProcessor(Info, Config, targetFolder)

  def scripts(concatPrefix:String)(paths: String*): play.api.templates.Html = {

    if (paths.length == 0) {
      Html("<!-- AssetLoader :: error : no paths to load -->")
    } else {
      val pathsAsFiles: List[File] = paths.map(p => new File("." + Info.filePath + "/" + p)).toList
      val allFiles = distinctFiles(pathsAsFiles: _*)
      val allJsFiles = typeFilter(".js", allFiles)
      val scripts = processor.process(concatPrefix,allJsFiles)
      val out = interpolate(AssetLoaderTemplate,
        "content" -> scripts.mkString("\n"),
        "files" -> allJsFiles.map(_.getName).mkString("\n\t"))
      Html(out)
    }
  }

  private lazy val Info: AssetsInfo = AssetsInfo("/assets", "/public")

  private lazy val Config: AssetsLoaderConfig = {

    val modeKey = Play.mode match {
      case play.api.Mode.Dev => "dev"
      case play.api.Mode.Test => "test"
      case play.api.Mode.Prod => "prod"
    }

    def bool(property: String, default: Boolean = false): Boolean = {
      val maybeBoolean = current.configuration.getBoolean("assetsLoader." + modeKey + "." + property)
      com.ee.log.Logger.debug(property + ": " + maybeBoolean)
      maybeBoolean.getOrElse(default)
    }

    AssetsLoaderConfig(bool("concatenate"), bool("minify"), bool("gzip"))
  }

  private lazy val targetFolder: String = {
    val Regex = """.*(target/.*?/classes).*""".r
    val Regex(path) = com.ee.utils.play.classesFolder().getAbsolutePath
    path + "/"
  }


  private val AssetLoaderTemplate =
    """<!-- Asset Loader -->
      |    <!--
      |    files:
      |    ${files}
      |    -->
      |    ${content}
      |<!-- End -->
    """.stripMargin
}