package com.ee.assets

import com.ee.assets.deployment.Deployer
import play.api.{Play, Configuration, Mode}
import com.google.javascript.jscomp.CompilerOptions
import play.api.templates.Html
import java.io.File
import com.ee.assets.exceptions.AssetsLoaderException
import com.ee.assets.transformers.Element
import java.net.URL

object SourceContext extends Enumeration {

  import play.api.Play.current

  type SourceContext = Value

  val Jar, Folder, Unknown = Value

  def apply(): SourceContext = {

    val target = Play.getFile("target")

    if (!target.exists()) {
      Unknown
    } else {
      if (folderHas(target, _.getName == "universal")) {
        Jar
      } else if (folderHas(target, _.getName.startsWith("scala-"))) {
        Folder
      } else Unknown
    }
  }

  def folderHas(parent: File, p: File => Boolean): Boolean = {
    parent.listFiles.exists(p)
  }
}


class NewLoader(deployer: Option[Deployer] = None, mode: Mode.Mode, config: Configuration, closureCompilerOptions: Option[CompilerOptions] = None) {


  def scripts(concatPrefix: String)(paths: String*): play.api.templates.Html = {

    //1: Get a list of all elements
    mode match {
      case Mode.Dev =>
    }


    Html("<script></script>")
  }


  private def toElements(paths: String*): Seq[Element] = {
    paths.map {
      p =>
        Play.resource(p).map {
          url => url.getProtocol match {
            case "jar" => {

              listAllChildrenFromJar(url)
            }
            case "file" => listAllChildrenFromFolder(url)
          }
        }
    }
    Seq.empty
  }

  private def listAllChildrenFromJar(url:URL) : Seq[Element] = {


  }


  private def listAllChildrenFromFolder(url: URL): Seq[Element] = {
    val root = new File(url.getPath)
    import com.ee.utils.file._
    val allFiles = distinctFiles(root)
    allFiles.map {
      f => Element(f.getPath, Some(readContents(f)))
    }
  }
}
