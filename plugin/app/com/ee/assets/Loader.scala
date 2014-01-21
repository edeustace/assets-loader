package com.ee.assets

import com.ee.assets.deployment.Deployer
import com.ee.assets.models.{Suffix, AssetsLoaderConfig, AssetsInfo}
import com.ee.assets.processors._
import com.ee.js.JavascriptCompiler
import com.ee.log.Logger
import com.ee.utils.file._
import com.ee.utils.string._
import java.io.{StringWriter, StringReader, File}
import play.api.{Play, Configuration, Mode}
import play.api.templates.Html
import com.google.javascript.jscomp.CompilerOptions

object Loader{

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""
  val CssTemplate = """<link rel="stylesheet" type="text/css" href="${src}"/>"""

  val AssetLoaderTemplate =
    """<!-- Asset Loader -->
      |    <!--
      |    files:
      |    ${files}
      |    -->
      |    ${content}
      |<!-- End -->
    """.stripMargin
}

/**
 * @param deployer
 * @param mode
 * @param config
 * @param closureCompilerOptions optional closure compiler options
 */
class Loader(deployer:Option[Deployer] = None, mode : Mode.Mode, config : Configuration, closureCompilerOptions : Option[CompilerOptions] = None) {

  private val jsProcessor: AssetProcessor =
    new SimpleFileProcessor(Info, JsConfig, assetsFolder, Loader.ScriptTemplate, ".js", minifyJs, loaderHash, deployer)

  private val cssProcessor: AssetProcessor =
    new SimpleFileProcessor(Info, CssConfig, assetsFolder, Loader.CssTemplate, ".css", minifyCss, loaderHash, deployer)

  def scripts(concatPrefix: String)(paths: String*): play.api.templates.Html = run(jsProcessor, concatPrefix)(paths: _*)

  def css(concatPrefix: String)(paths: String*): play.api.templates.Html = run(cssProcessor, concatPrefix)(paths: _*)

  private def run(processor: AssetProcessor, concatPrefix: String)(paths: String*): play.api.templates.Html = {
    if (paths.length == 0) {
      Html("<!-- AssetLoader :: error : no paths to load -->")
    } else {
      import Play.current

      val pathsAsFiles: List[File] = paths.map(p => Play.getFile( s".${Info.filePath}${File.separator}$p") ).toList
      Logger.trace(s"paths: $pathsAsFiles")
      val allFiles = distinctFiles(pathsAsFiles: _*)
      val typedFiles = typeFilter(processor.suffix, allFiles)
      val assets = processor.process(concatPrefix, typedFiles)
      val out = interpolate(Loader.AssetLoaderTemplate,
        "content" -> assets.mkString("\n"),
        "files" -> typedFiles.map(_.getName).mkString("\n\t"))
      Html(out)
    }
  }

  def minifyCss(file: File, destination: String) {
    Logger.debug( s"[minifyCss]  $file destination: $destination")
    val contents = readContents(file)
    val compressor = new com.yahoo.platform.yui.compressor.CssCompressor(new StringReader(contents))
    val writer = new StringWriter()
    compressor.compress(writer, 0)
    writeToFile(destination, writer.toString)
  }

  def minifyJs(file: File, destination: String) {
    Logger.debug( s"[minifyJs]  $file  destination: $destination")
    val contents = readContents(file)
    val out = JavascriptCompiler.minify(contents, None, closureCompilerOptions)
    writeToFile(destination, out)
  }



  /** Use only the name for hashing on production as the file will not change */
  private def loaderHash(files:List[File]) : String = {
    import com.ee.utils.file._

    val fileToStringFn : (File => String) = mode match{
      case Mode.Prod => {
        (f : File )=> {
          Logger.debug("return simple file name for Production mode")
          f.getName
        }
      }
      case _ => (f : File )=> f.getName + "_" + f.lastModified
    }
    hash(files, fileToStringFn)
  }

  private lazy val Info: AssetsInfo = AssetsInfo("/assets", "/public")

  private lazy val CssConfig : AssetsLoaderConfig = AssetsLoaderConfig.fromAppConfiguration( mode.toString.toLowerCase, Suffix.css, config)
  private lazy val JsConfig : AssetsLoaderConfig = AssetsLoaderConfig.fromAppConfiguration(mode.toString.toLowerCase, Suffix.js, config)

  private lazy val assetsFolder: File = com.ee.utils.play.assetsFolder

}