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
      ${mainTemplate(transformed.map(_.path).mkString("\n"), tags.mkString("\n"))}
      <!--
      Elements raw
      -----------------
      <div>${elements.map(_.path).mkString("<br/>")}</div>
      Elements processed
      ------------------
      <div>${transformed.map(_.path).mkString("<br/>")}</div>
      -->
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
    val write = new Writer(writeToGeneratedFolder)
    val gzipWrite = new GzipperWriter(pathToFile)
    val toWebPath = new FileToWebPath(Info)

    Seq(
      Some(read),
      if (config.concatenate) Some(concat) else None,
      if (config.minify) Some(minify) else None,
      if (config.gzip) Some(gzipWrite) else Some(write),
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
        Play.resource(publicDir(p)).map {
          url => url.getProtocol match {
            case "jar" => listAllChildrenFromJar(filter)(url)
            case "file" => {
              val absolutePathElements = listAllChildrenFromFolder(filter)(url)

              def trimPath(elementPath: String): String = {
                logger.trace(s"[trimPaths]: $elementPath, root: $p, url: ${url.getPath}, ${url.getFile}")
                val publicPathIndex = elementPath.indexOf(publicDir(p))
                elementPath.substring(publicPathIndex)
              }
              absolutePathElements.map {
                e =>
                  e.copy(trimPath(e.path))
              }
            }
            case _ => throw new AssetsLoaderException(s"unknown file protocol: ${url.getProtocol} ")
          }
        }.getOrElse {
          logger.warn(s"no resource for: $p")
          Seq()
        }
    }.flatten
  }

  private def listAllChildrenFromJar(fileTypeFilter: String => Boolean)(url: URL): Seq[Element] = {
    logger.trace(s"[listAllChildrenFromJar] url: $url")
    val jarPath = url.getPath().substring(5, url.getPath().indexOf("!"))

    require(url.getFile.contains("!/"))

    val filePath = url.getFile.split("!/")(1)
    logger.trace(s"[listAllChildrenFromJar] path: $filePath")
    val jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))

    def folders(p: String) = p.endsWith("/")
    val noFolders = (folders(_: String) == false)
    def startsWith(p: String) = p.startsWith(filePath)
    def finalFilter(p: String) = startsWith(p) && noFolders(p) && fileTypeFilter(p)

    jar.listChildrenInJar(jarFile, finalFilter).map {
      Element(_, None)
    }
  }


  private def listAllChildrenFromFolder(fileTypeFilter: String => Boolean)(url: URL): Seq[Element] = {
    logger.trace(s"[listAllChildrenFromFolder] : $url")
    val root = new File(url.getPath)
    import com.ee.utils.file.distinctFiles
    val allFiles = distinctFiles(root)
    def isFile(f: File) = f.isFile
    def finalFn(f: File) = isFile(f) && fileTypeFilter(f.getName)

    allFiles.filter(finalFn).map {
      f => {
        logger.trace(s"file path: ${f.getPath}")
        Element(f.getPath)
      }
    }
  }
}
