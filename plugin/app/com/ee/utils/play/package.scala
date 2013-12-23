package com.ee.utils

import _root_.play.api.Play
import java.io._
import com.ee.log.Logger
import com.ee.assets.exceptions.AssetsLoaderException

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
    if (new File("target/universal").exists) {
      Logger.debug("initialising exploded jar...")
      explodedJarFolder
    } else {
      Logger.debug("initialising classes folder...")
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

      for {
        scriptFile <- binFolder.listFiles().filterNot(_.getName.endsWith(".bat")).headOption
        if (scriptFile.exists)
        name <- Some(scriptFile.getName)
        appJar <- libFolder.listFiles().filter(_.getName.startsWith(s"$name.$name-")).headOption
      } yield appJar
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
      val jarPath = jar.getAbsolutePath
      import scala.sys.process._
      "pwd".!
      Logger.debug(s"jar path: $jarPath")

      val destination = "target/universal/stage"

      def currentDir = "pwd".!!.trim
      val command =  s"jar xf $jarPath public"

      if( new File("public").exists ){
        def devModeTidyUp = {
          //Note: to prevent generated files from being written to the app's public folder
          //in dev mode, we back up public, expand the jar's public folder and then move it.
          //the we tidy up.
          grizzled.file.util.copyTree("public", "___backup_public")
          command.!
          grizzled.file.util.copyTree("public", s"$destination/public")
          grizzled.file.util.deleteTree("public")
          grizzled.file.util.copyTree("___backup_public", "public")
          grizzled.file.util.deleteTree("___backup_public")
          Logger.debug(s"running command: $command")
        }
        devModeTidyUp
        val out = new File(currentDir + "/" + destination )
        out
      } else {
        command.!
        val out = new File(currentDir)
        Logger.debug(s"exploded folder: ${out.getAbsolutePath}")
        out
      }
  }

}