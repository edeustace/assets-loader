package com.ee.assets.processors

import com.ee.assets.deployment.ContentInfo
import com.ee.assets.deployment.Deployer
import com.ee.assets.exceptions.AssetsLoaderException
import com.ee.assets.models.AssetsInfo
import com.ee.assets.models.AssetsLoaderConfig
import com.ee.log.Logger
import com.ee.utils.play.Separator
import com.ee.utils.file.{nameAndSuffix, readContents, writeToFile}
import com.ee.utils.string._
import java.io._

class SimpleFileProcessor(
                           info: AssetsInfo,
                           config: AssetsLoaderConfig,
                           assetsFolder: File,
                           srcTemplate: String,
                           val suffix: String,
                           minify: (File, String) => Unit,
                           hash: List[File] => String,
                           deployer: Option[Deployer]) extends AssetProcessor {


  type ConcatenatedName = String

  /** Process the files
    * @param files - these files are in the static config folder as defined AssetsInfo.filePath
    *              Typically this is the '/public' folder in the app.
    *
    *              Note: The first thing we do is point to the equivalent files in the target folder.
    *              All processing will happen against these files in target.
    */
  def process(prefix: String, files: List[File]): List[String] = {
    def onlyRightType: Boolean = files.filterNot("." + nameAndSuffix(_)._2 == suffix).length == 0
    require(onlyRightType, "all files must be " + suffix + " files")

    implicit val concatenatedName: ConcatenatedName = prefix + "-" + hash(files) + suffix
    Logger.debug("Concatentated name: " + concatenatedName)
    val filesInTargetFolder = files.map(toFileInTargetFolder)
    processFileList(info.filePath, filesInTargetFolder)
  }

  /** point to equivalent file in the compiled destination not the source folder
    */
  private def toFileInTargetFolder(f: File): File = {

    def filePathFolder: File = if (info.filePath.startsWith("/")) new File("." + info.filePath) else new File(info.filePath)
    def currentParent = filePathFolder

    val relative = relativePath(f, currentParent)
    val destination = makePath(assetsFolder, info.filePath, relative)
    Logger.debug("target file: " + destination)

    val targetFile = new File(destination)
    if (!targetFile.exists()) {
      Logger.error("Error - the target file doesn't exist: " + destination)
    }
    targetFile
  }

  private def processFileList(path: String, files: List[File])(implicit concatenatedName: ConcatenatedName): List[String] = {

    val processed: Option[List[File]] = for {
      concatenated <- concat(path, files)
      minified <- minify(concatenated)
      gzipped <- gzip(minified)
    } yield {
      gzipped
    }

    def contentType = if (suffix == ".js") "text/javascript" else "text/css"

    processed.map {
      files =>
        files.map {
          f: File =>

            Logger.debug("[processFileList] ->")
            val relative = relativePath(f, assetsFolder)

            def pointToLocalFile: String = {
              Logger.debug("relative: " + relative)
              val withWebPath = __/|/(relative.replace(info.filePath, info.webPath))
              Logger.debug("web path: " + withWebPath)
              scriptTag(withWebPath)
            }

            def deployFile(d: Deployer): String = {
              val trimmed = __/|/(relative.replace(info.filePath, ""))
              Logger.debug("calling deploy with: " + trimmed)

              def stream : InputStream = if (config.gzip) bufferedInputStream(f) else byteArrayStream(f)

              d.deploy(trimmed, f.lastModified(), stream, ContentInfo(contentType, if (config.gzip) Some("gzip") else None)) match {
                case Right(path) => scriptTag(path)
                case Left(error) => throw new AssetsLoaderException("Error deploying: " + error)
              }
            }

            if(config.deploy)
              deployer.map(deployFile).getOrElse(pointToLocalFile)
            else
              pointToLocalFile
        }
    }.getOrElse(List())
  }

  private def bufferedInputStream(file:File): InputStream = new BufferedInputStream(new FileInputStream(file))

  private def byteArrayStream(file:File) : ByteArrayInputStream = new ByteArrayInputStream(readContents(file).getBytes("UTF-8"))


  private def relativePath(child: File, parent: File): String = {
    val childFullPath = child.getCanonicalPath
    val parentFullPath = parent.getCanonicalPath

    if (childFullPath.startsWith(parentFullPath)) {
      childFullPath.replace(parentFullPath, "")
    } else {
      throw new RuntimeException("Error getting relative path the child isn't actually a child: " + childFullPath + " parent: " + parentFullPath)
    }
  }


  private def concat(path: String, files: List[File])(implicit concatenatedName: ConcatenatedName): Option[List[File]] = if (config.concatenate) {

    val destination = makePath(assetsFolder, info.filePath, concatenatedName)

    Logger.debug("[concat] -> destination: " + destination)

    if (!(new File(destination).exists)) {
      concatFiles(files, destination)
    }
    else {
      Logger.debug("[concat] file already exists: " + new File(destination).getAbsolutePath)
    }
    Some(List(new File(destination)))
  } else {
    Some(files)
  }

  private def minify(files: List[File]): Option[List[File]] = if (config.minify) {
    processFilesInList(files, f => com.ee.utils.file.nameAndSuffix(f)._1 + ".min" + suffix, minify)
  } else {
    Some(files)
  }

  private def gzip(files: List[File]): Option[List[File]] = if (config.gzip) {
    processFilesInList(files, f => com.ee.utils.file.nameAndSuffix(f)._1 + ".gz" + suffix, gzipFile)
  } else {
    Some(files)
  }

  private def processFilesInList(files: List[File], nameFile: (File => String), process: ((File, String) => Unit)): Option[List[File]] = {
    val processed = files.map {
      f: File =>
        val processedName = nameFile(f)
        val destination = f.getAbsolutePath.replace(f.getName, processedName)

        if (!(new File(destination).exists)) {
          Logger.debug("[processFilesInList] File doesn't exist -> processing now...")
          process(f, destination)
        } else {
          Logger.debug("[processFilesInList] File exists - return this file - don't process")
        }
        new File(destination)
    }
    Some(processed)
  }


  private def concatFiles(files: List[File], destination: String) {
    Logger.debug("[concatFiles] destination: " + destination)
    Logger.debug("[concatFiles] files: " + files)
    val fileNames = files.map(_.getName).mkString
    val contents = files.map(f => {
      try {
        readContents(f).mkString("\n")
      } catch {
        case e: Throwable => {
          Logger.error("An exception occurred reading contents from " + f.getName)
          throw new AssetsLoaderException("concatFiles: " + fileNames, e)
        }
      }
    })

    try {
      writeToFile(destination, contents)
    } catch {
      case e: Throwable => {
        Logger.error("An exception occurred concatenating: " + fileNames)
        throw new AssetsLoaderException("concatFiles: " + fileNames, e)
      }
    }
  }


  private def gzipFile(file: File, destination: String) {
    Logger.debug("[gzipFile] " + file + " destination: " + destination)
    val contents = readContents(file)
    com.ee.utils.gzip.gzip(contents, destination)
  }

  private def __/|/(s: String): String = s.replace("/./", "/").replace("//", "/")

  private def scriptTag(url: String): String = interpolate(srcTemplate, ("src", url))

  private def trim_/(raw: String): String = if (raw.startsWith("/")) raw.substring(1, raw.length) else raw

  private def trimLast_/(raw: String): String = if (raw.endsWith("/")) raw.substring(0, raw.length - 1) else raw

  private def trimSeparator(path:String) : String =  (trim_/ _ andThen trimLast_/ _)(path)

  private def makePath(f : File, s: String*): String = {
    val trimmed = s.map(trimSeparator)
    new File( f.getPath + Separator + trimmed.mkString(Separator)).getPath
  }

}
