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
    classesFolder orElse explodedJarFolder getOrElse {
      throw new AssetsLoaderException("Error can't find a class folder or exploded jar folder")
    }
  }

  private def classesFolder(): Option[File] = {
    val target: File = new File("target")

    if (!target.exists) {
      None
    } else {
      target.listFiles.toList.find(_.getName.startsWith("scala-")) match {
        case Some(scalaFolder) => {
          val path = List(target.getName, scalaFolder.getName, "classes").mkString(Separator)
          val file: File = new File(path)
          if (file.exists) Some(file) else throw new RuntimeException("can't find scala-${version}/classes folder")
        }
        case _ => None
      }
    }
  }

  private def getAppJarName: Option[String] = {
    val configuredJarfile = Play.current.configuration.getString("assetsLoader.prod.jarfile")
    Logger.debug(s"Configured jar file: $configuredJarfile")
    configuredJarfile.orElse(throw new RuntimeException("If you are running in production mode, you must specify the relative path (from start script -> jar)"))
  }

  /**
   * If a classes folder can't be found it is assumed that the app is run in prod mode aka its been zipped up using `play dist`.
   * The name of the jar to explode can be specfied in the config file: assets.loader.prod.jarfile = XXX
   * otherwise we try and find a the jar that contains the assets. To find this we parse the `start` script and look for the last jar
   * listed on the classpath and expand that.
   * @return
   */
  private def explodedJarFolder: Option[File] = getAppJarName.map {
    name =>
      Logger.debug(s"name: $name")
      val jarPath = Play.current.getFile(name).getAbsolutePath
      import scala.sys.process._
      Logger.debug(s"jar path: $jarPath")
      s"jar xf $jarPath public".!
      new File(".")
  }

}