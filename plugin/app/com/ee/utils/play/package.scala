package com.ee.utils

import _root_.play.api.Play
import _root_.play.api.Play.current
import java.io._
import com.ee.log.Logger
import com.ee.assets.exceptions.AssetsLoaderException
import java.util.jar.{JarEntry, JarFile}

package object play {

  private var cachedAssetsFolder: File = null

  val Separator = sys.env.get("file.separator").getOrElse("/")

  def assetsFolder(): File = {
    if (cachedAssetsFolder == null) {
      cachedAssetsFolder = initAssetsFolder
      Logger.debug(s"[assetsFolder] folder is initialized to: ${cachedAssetsFolder.getAbsolutePath}")
    }
    cachedAssetsFolder
  }

  private def initAssetsFolder: File = {

    Logger.trace(s"App root: ${Play.getFile(".")}")

    if( Play.getFile("lib").exists){
      Logger.debug("This is a dist/stage structure: initialising exploded jar...")
      explodedJarFolder
    } else if (Play.getFile("target/universal").exists) {
      Logger.debug("This is a stage structure: initialising exploded jar...")
      explodedJarFolder
    } else {
      Logger.debug("initialising classes folder...")
      Logger.trace(s"folder: $classesFolder")
      classesFolder
    }
  }.getOrElse(throw new AssetsLoaderException("Error can't find a class folder or exploded jar folder"))


  private def classesFolder(): Option[File] = {
    val target: File = new File("target")

    if (!target.exists) {
      None
    } else {
      target.listFiles.toList.find(_.getName.startsWith("scala-")) match {
        case Some(scalaFolder) => {
          Logger.debug(s"found ${scalaFolder.getName}")
          val path = List(target.getName, scalaFolder.getName, "classes").mkString(Separator)
          val file: File = new File(path)
          if (file.exists) {
            Some(file)
          } else throw new RuntimeException("can't find 'target/scala-${version}/classes' folder")
        }
        case _ => None
      }
    }
  }

  private def getAppJar: Option[File] = {

    /**
     * the script name is the app name, so read it then use it to return the app jar from the lib folder.
     * @return
     */
    def loadFromScriptName: Option[File] = {
      val binFolder: File = Play.current.getFile("bin")
      val libFolder: File = Play.current.getFile("lib")
      Logger.debug(s"using bin folder: ${binFolder.getAbsolutePath}")
      Logger.debug(s"using lib folder: ${libFolder.getAbsolutePath}")

      val scriptFile = binFolder.listFiles().filterNot(_.getName.endsWith(".bat")).headOption
      Logger.trace(s"scriptFile: ${scriptFile.map(_.getName).getOrElse("doesn't exist")} ")
      val jars = libFolder.listFiles()
      Logger.trace(s"jars: ${jars.mkString("\n")} ")


      for {
        scriptFile <- binFolder.listFiles().filterNot(_.getName.endsWith(".bat")).headOption
        if (scriptFile.exists)
        name <- Some(scriptFile.getName)
        appJar <- libFolder.listFiles().filter(_.getName.contains(s".$name-")).headOption
      } yield {
        appJar
      }

    }

    val configuredJarfile = Play.current.configuration.getString("assetsLoader.prod.jarfile")

    Logger.debug(s"Configured jar file: $configuredJarfile")

    configuredJarfile.map {
      f =>
        Play.current.getFile(s"lib/$f")

    }.orElse(loadFromScriptName)
  }

  /**
   * If a classes folder can't be found it is assumed that the app is run in prod mode aka its been zipped up using `play dist`.
   * The name of the jar to explode can be specfied in the config file: assets.loader.prod.jarfile = XXX
   * otherwise we try and find a the jar that contains the assets, expand it using the `jar xf` command and then return the path
   * @return
   */
  private def explodedJarFolder: Option[File] = getAppJar.map {
    jar =>
      import com.ee.utils.jar._
      val jarPath = jar.getAbsolutePath
      Logger.debug(s"jar path: $jarPath")
      import grizzled.file.util

      val publicFolder = Play.getFile("public")

      if(publicFolder.exists){
        util.deleteTree(publicFolder)
      }
      
      def filter(s:String) = s.startsWith("public")

      extractJar(jar, Play.getFile("."), filter)
  }

}