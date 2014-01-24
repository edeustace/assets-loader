package com.ee.assets

import com.ee.assets.deployment.Deployer
import com.ee.assets.exceptions.AssetsLoaderException
import com.ee.assets.models.{Suffix, AssetsLoaderConfig, AssetsInfo}
import com.ee.assets.transformers._
import com.ee.log.Logger
import com.ee.utils.jar
import com.google.javascript.jscomp.CompilerOptions
import java.io.File
import java.net.{URLDecoder, URL}
import java.util.jar.JarFile
import play.api.templates.Html
import play.api.{Play, Configuration, Mode}
import com.ee.assets.paths.PathResolver

class Loader(deployer: Option[Deployer] = None, mode: Mode.Mode, config: Configuration, closureCompilerOptions: Option[CompilerOptions] = None) {

  private lazy val JsConfig: AssetsLoaderConfig = AssetsLoaderConfig.fromAppConfiguration(mode.toString.toLowerCase, Suffix.js, config)
  private lazy val CssConfig: AssetsLoaderConfig = AssetsLoaderConfig.fromAppConfiguration(mode.toString.toLowerCase, Suffix.css, config)

  private lazy val Info: AssetsInfo = AssetsInfo("assets", "public")

  val generatedDir = com.ee.utils.play.generatedFolder

  val logger = Logger("new-loader")

  logger.warn("Note: deployer is currently disabled")

  def scripts(concatPrefix: String)(paths: String*): play.api.templates.Html = {


    val key = s"$concatPrefix-${paths.sortWith(_ < _).mkString(",").hashCode}"

    logger.info(s"[js] mode: $mode")

    TagCache.js.get(key) match {
      case Some(tag) if mode != Mode.Dev => {
        logger.info(s"[js] Found cached tag for: $concatPrefix - ${paths.mkString(",")}")
        tag
      }
      case _ => {
        logger.debug(s"[js] create tag for: $concatPrefix - ${paths.mkString(",")}")
        val tags = prepareTags(
          s => s.endsWith(".js"),
          buildJsTransformations(concatPrefix),
          Templates.script
        )(paths: _*)

        TagCache.js.put(key, tags)
        tags
      }
    }
  }

  def css(concatPrefix: String)(paths: String*): play.api.templates.Html = {
    val key = s"$concatPrefix-${paths.sortWith(_ < _).mkString(",").hashCode}"

    logger.info(s"[css] mode: $mode")

    TagCache.css.get(key) match {

      case Some(tag) if mode != Mode.Dev => {
        logger.debug(s"[css] Found cached tag for: $concatPrefix - ${paths.mkString(",")}")
        tag
      }
      case _ => {
        logger.info(s"[css] create tag for: $concatPrefix - ${paths.mkString(",")}")
        val tags = prepareTags(
          s => s.endsWith(".css"),
          buildCssTransformations(concatPrefix),
          Templates.css
        )(paths: _*)

        TagCache.css.put(key, tags)
        tags
      }
    }
  }

  private def prepareTags(
                           filetypeFilter: String => Boolean,
                           sequence: Seq[Transformer],
                           tagFn: String => String)(paths: String*): Html = {
    val elements = toElements(filetypeFilter)(paths: _*)
    logger.trace(s"Initialising generated assets folder to: ${generatedDir.getAbsolutePath}")
    val transformation = new TransformationSequence(sequence: _*)
    val transformed = transformation.run(elements)
    import com.ee.assets.Templates._
    val tags = transformed.map(e => tagFn(e.path))

    val out = s"""
      <!--
      Request:
      --------
      ${paths.mkString("\n")}

      Elements raw:
      -----------------
      ${elements.map(_.path).mkString("\n")}

      -->
      <!-- tags -->
      ${mainTemplate(transformed.map(_.path).mkString("\n"), tags.mkString("\n"))}
    """
    Html(out)
  }


  private def buildCssTransformations(concatPrefix: String) = buildTransformations(concatPrefix, "css", CssConfig, new CssMinifier())


  private def buildJsTransformations(concatPrefix: String) = buildTransformations(concatPrefix, "js", JsConfig, new JsMinifier(closureCompilerOptions))


  private def buildTransformations(
                                    concatPrefix: String,
                                    suffix: String,
                                    config: AssetsLoaderConfig,
                                    minify: Transformer) = {
    val read = new PlayResourceReader
    val namer = new CommonRootNamer(concatPrefix, suffix)
    val concat = new Concatenator(namer)
    val toWebPath = new FileToWebPath(Info)
    val write = if (!config.concatenate && !config.minify && !config.gzip) {
      None
    } else {
      if (config.gzip) Some(new GzipperWriter(pathToFile))
      else
        Some(new Writer(writeToGeneratedFolder))
    }

    Seq(
      Some(read),
      if (config.concatenate) Some(concat) else None,
      if (config.minify) Some(minify) else None,
      write,
      Some(toWebPath)
    ).flatten
  }

  def writeToGeneratedFolder(path: String, contents: String): Unit = {
    import com.ee.utils.file
    val finalPath = new File(s"${generatedDir}${File.separator}$path").getCanonicalPath
    logger.trace(s"final path for generated asset: $finalPath")
    file.writeToFile(finalPath, contents)
  }

  def pathToFile(p: String): File = new File(s"$generatedDir${File.separator}/$p")


  private def toElements(filter: String => Boolean)(paths: String*): Seq[Element] = {

    import play.api.Play.current
    logger.debug(s"[toElements]: $paths")
    def publicDir(p: String) = s"${Info.filePath}/$p"
    paths.map {
      p =>

        def toUrl(p: String): URL = Play.resource(p).getOrElse {
          throw new AssetsLoaderException(s"[toElements] can't load path: $p")
        }

        val paths = PathResolver.resolve(publicDir(p), toUrl)
        val filtered = paths.filter(filter)
        logger.trace(s"[toElements]: \n${filtered.mkString("\n")}")
        filtered.map(Element(_))
    }.flatten
  }
}

