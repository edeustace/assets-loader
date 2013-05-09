package com.ee.assets.processors

import com.ee.assets.models.AssetsLoaderConfig
import com.ee.assets.models.AssetsInfo
import com.ee.log.Logger
import java.io.File
import com.ee.utils.string._
import com.ee.utils.file._
import com.ee.js.JavascriptCompiler

class SimpleFileProcessor(info: AssetsInfo, config: AssetsLoaderConfig, targetFolder: String) extends AssetProcessor {

  val ScriptTemplate = """<script type="text/javascript" src="${src}"></script>"""

  type Hash = String
  type ConcatenatedName = String

  /** Process the js files
    * @param jsFiles - these files are in the static config folder as defined AssetsInfo.filePath
    *                Typically this is the '/public' folder in the app.
    *
    *                Note: The first thing we do is point to the equivalent files in the target folder.
    *                All processing will happen against these files in target.
    */
  def process(prefix : String, jsFiles: List[File]): List[String] = {
    def onlyJs: Boolean = jsFiles.filterNot(nameAndSuffix(_)._2 == "js").length == 0
    require(onlyJs, "all files must be .js files")

    implicit val concatenatedName : ConcatenatedName = prefix + "-" + hash(jsFiles) + ".js"

    val filesInTargetFolder = jsFiles.map(toFileInTargetFolder)
    processFileList(info.filePath, filesInTargetFolder)
  }

  /** point to equivalent file in the compiled destination not the source folder
    */
  private def toFileInTargetFolder(f: File): File = {

    def filePathFolder: File = if (info.filePath.startsWith("/")) new File("." + info.filePath) else new File(info.filePath)
    def currentParent = filePathFolder

    val relative = relativePath(f, currentParent)
    val destination = makePath(targetFolder, info.filePath, relative)
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

    processed.map {
      files =>
        files.map {
          f: File =>
            Logger.debug("[processFileList] ->")
            val relative = relativePath(f, target)
            Logger.debug("relative: " + relative)
            val withWebPath = __/|/(relative.replace(info.filePath, info.webPath))
            Logger.debug("web path: " + withWebPath)
            scriptTag(withWebPath)
        }
    }.getOrElse(List())
  }

  private def relativePath(child: File, parent: File): String = {
    val childFullPath = child.getCanonicalPath
    val parentFullPath = parent.getCanonicalPath

    if (childFullPath.startsWith(parentFullPath)) {
      childFullPath.replace(parentFullPath, "")
    } else {
      throw new RuntimeException("Error getting relative path the child isn't actually a child: " + childFullPath + " parent: " + parentFullPath)
    }
  }


  private def concat(path: String, files: List[File])(implicit concatenatedName: ConcatenatedName ): Option[List[File]] = if (config.concatenate) {

    val destination = makePath(targetFolder, info.filePath, concatenatedName)

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
    processFilesInList(files, f => com.ee.utils.file.nameAndSuffix(f)._1 + ".min.js", minifyFile)
  } else {
    Some(files)
  }

  private def gzip(files: List[File]): Option[List[File]] = if (config.gzip) {
    processFilesInList(files, f => com.ee.utils.file.nameAndSuffix(f)._1 + ".gz.js", gzipFile)
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
    val contents = files.map(f => readContents(f)).mkString("\n")
    Logger.debug("[concatFiles] destination: " + destination)
    Logger.debug("[concatFiles] files: " + files)
    writeToFile(destination, contents)
  }

  private def minifyFile(file: File, destination: String) {
    Logger.debug("[minifyFile]  " + file + " destination: " + destination)
    val contents = readContents(file)
    val out = JavascriptCompiler.minify(contents, None)
    writeToFile(destination, out)
  }

  private def gzipFile(file: File, destination: String) {
    Logger.debug("[gzipFile] " + file + " destination: " + destination)
    val contents = readContents(file)
    com.ee.utils.gzip.gzip(contents, destination)
  }

  private def __/|/(s: String): String = s.replace("/./", "/").replace("//", "/")

  private def scriptTag(url: String): String = interpolate(ScriptTemplate, ("src", url))

  private def trim_/(raw: String): String = if (raw.startsWith("/")) raw.substring(1, raw.length) else raw

  private def trimLast_/(raw: String): String = if (raw.endsWith("/")) raw.substring(0, raw.length - 1) else raw

  private def makePath(s: String*): String = {
    val trimmed = s.map(trim_/ _ andThen trimLast_/ _)
    trimmed.mkString("/")
  }

  private def target: File = new File(targetFolder)
}
