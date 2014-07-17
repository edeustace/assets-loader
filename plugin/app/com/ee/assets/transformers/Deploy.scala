package com.ee.assets.transformers

import com.ee.assets.deployment.{ContentInfo, Deployer}
import com.ee.assets.models.AssetsInfo
import com.ee.log.Logger
import java.io.{InputStream, ByteArrayInputStream}

abstract class BaseDeploy[A](d: Deployer, info: AssetsInfo) extends Transformer[A, Unit] {

  protected def logger: org.slf4j.Logger

  protected def getInputStream(contents: A): InputStream

  protected def encoding: Option[String]

  override def run(elements: Seq[Element[A]]): Seq[PathElement] = {
    val pathElements = elements.map {
      e =>
        def contentType = if (e.path.endsWith(".js")) "text/javascript" else "text/css"
        val is: InputStream = getInputStream(e.contents)
        d.deploy(e.path, e.lastModified.getOrElse(0), is, ContentInfo(contentType, encoding)) match {
          case Right(p) => Some(PathElement(p))
          case Left(err) => {
            logger.warn(s"Error deploying: ${e.path}")
            None
          }
        }
    }.flatten

    if(info.isExternalHost){
      pathElements.map{ pathElement =>
        d.resolve(pathElement, info)
      }
    }
    else{
      pathElements
    }
  }

}


object StringDeploy {
  def apply(d: Deployer, info: AssetsInfo) = new StringDeploy(d, info).run _
}

class StringDeploy(d: Deployer, info: AssetsInfo) extends BaseDeploy[String](d, info) {
  override protected def logger = Logger("deploy.string")

  override def getInputStream(c: String): InputStream = new ByteArrayInputStream(c.getBytes("UTF-8"))

  override protected def encoding: Option[String] = None
}

object ByteArrayDeploy {
  def apply(d: Deployer, info: AssetsInfo) = new ByteArrayDeploy(d, info).run _
}

class ByteArrayDeploy(d: Deployer, info: AssetsInfo) extends BaseDeploy[Array[Byte]](d, info) {
  override protected def logger = Logger("deploy.byte.array")

  override def getInputStream(c: Array[Byte]): InputStream = new ByteArrayInputStream(c)

  override protected def encoding: Option[String] = Some("gzip")
}

