package com.ee.assets

import com.ee.assets.deployment.Deployer
import com.ee.assets.models.{Suffix, AssetsLoaderConfig, AssetsInfo}
import com.ee.assets.paths.PathResolver
import com.ee.assets.transformers._
import com.ee.log.Logger
import com.google.javascript.jscomp.CompilerOptions
import java.io.File
import java.net.URL
import play.api.templates.Html
import play.api.{Play, Configuration, Mode}

/**
 *
 * @param deployer
 * @param mode
 * @param config
 * @param closureCompilerOptions
 * @param info - path info that maps the web path (when loading the assets via the server) -> file path (the location of the files in the project)
 */
class Loader(
              deployer: Option[Deployer] = None,
              mode: Mode.Mode,
              config: Configuration,
              closureCompilerOptions: Option[CompilerOptions] = None,
              info: AssetsInfo = AssetsInfo("assets", "public")) {

  private lazy val JsConfig: AssetsLoaderConfig = validateConfig(AssetsLoaderConfig.fromAppConfiguration(mode.toString.toLowerCase, Suffix.js, config))
  private lazy val CssConfig: AssetsLoaderConfig = validateConfig(AssetsLoaderConfig.fromAppConfiguration(mode.toString.toLowerCase, Suffix.css, config))

  private def validateConfig(c: AssetsLoaderConfig): AssetsLoaderConfig = {
    if (c.deploy && deployer.isEmpty) {
      logger.warn(s"Deployment has been enabled but no deployer has been specified - setting deploy to false. Original config: $c")
      c.copy(deploy = false)
    } else {
      c
    }
  }

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
          Templates.script,
          JsConfig.addHints
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
          Templates.css,
          CssConfig.addHints
        )(paths: _*)

        TagCache.css.put(key, tags)
        tags
      }
    }
  }

  private def prepareTags(
                           filetypeFilter: String => Boolean,
                           transformationFn: Seq[Element[Unit]] => Seq[Element[Unit]],
                           tagFn: String => String,
                           addHints: Boolean)(paths: String*): Html = {

    def transformAndCreateHtml(elements: Seq[Element[Unit]]): Html = {
      logger.trace(s"Initialising generated assets folder to: ${generatedDir.getAbsolutePath}")
      val transformed = transformationFn(elements)
      import com.ee.assets.Templates._
      val tags = transformed.map(e => tagFn(e.path))

      val tagString = tags.mkString("\n")
      val pathString = transformed.map(_.path).mkString("\n")

      def hinted = s"""
      <!--
      [assets-loader] hints
      (you can disable these by adding `addHints: false` to your conf)

      url:
      $pathString

      Request:
      --------
      ${paths.mkString("\n")}

      Found Elements:
      -----------------
      ${elements.map(_.path).mkString("\n")}

      -->

      $tagString
      """

      def plain = tags.mkString("\n")

      val out = if (addHints) hinted else plain
      Html(out)
    }

    toElements(filetypeFilter)(paths: _*) match {
      case Nil => if(addHints) Html(s"<!-- [assets-loader] warning: missing ${paths.mkString(",")} -->") else Html("")
      case e: Seq[Element[Unit]] => transformAndCreateHtml(e)
    }

  }


  private def buildCssTransformations(concatPrefix: String) = buildTransformations(concatPrefix, "css", CssConfig, new CssMinifier())


  private def buildJsTransformations(concatPrefix: String) = buildTransformations(concatPrefix, "js", JsConfig, new JsMinifier(closureCompilerOptions))


  private def buildTransformations(
                                    concatPrefix: String,
                                    suffix: String,
                                    config: AssetsLoaderConfig,
                                    minify: Transformer[String, String]): Seq[Element[Unit]] => Seq[Element[Unit]] = {
    val read = new PlayResourceReader
    val namer = new CommonRootNamer(concatPrefix, suffix)
    val concat = new Concatenator(namer)
    val toWebPath = new FileToWebPath(info)
    val gzip = new Gzip()
    val stringWriter = if (config.deploy) new StringDeploy(deployer.get).run _ else new Writer(writeToGeneratedFolder).run _ andThen toWebPath.run _
    val byteWriter = if (config.deploy) new ByteArrayDeploy(deployer.get).run _ else new ByteArrayWriter(pathToFile).run _ andThen toWebPath.run _
    val builder = new TransformationBuilder(read.run, concat.run, gzip.run, minify.run, stringWriter, byteWriter, toWebPath.run)
    builder.build(config)
  }

  def writeToGeneratedFolder(path: String, contents: String): Unit = {
    import com.ee.utils.file
    val finalPath = new File(s"${generatedDir}${File.separator}$path").getCanonicalPath
    logger.trace(s"final path for generated asset: $finalPath")
    file.writeToFile(finalPath, contents)
  }

  def pathToFile(p: String): File = new File(s"$generatedDir${File.separator}/$p")


  private def toElements(filter: String => Boolean)(paths: String*): Seq[Element[Unit]] = {

    import play.api.Play.current
    logger.debug(s"[toElements]: $paths")
    def publicDir(p: String) = s"${info.filePath}/$p"

    val pathsAndUrls: Seq[(String, URL)] = paths.map {
      p =>
        val public = publicDir(p)
        Play.resource(public).map((public, _)).orElse {
          logger.warn(s"[toElements] Can't find resource: $p")
          None
        }
    }.flatten



    pathsAndUrls.map {
      t: (String, URL) =>

        val paths = PathResolver.resolve(t._1, t._2)
        val filtered = paths.filter(filter)
        logger.trace(s"[toElements]: \n${filtered.mkString("\n")}")
        filtered.map(PathElement(_))
    }.flatten
  }
}


