package com.ee.assets.deployment

import java.io.InputStream

import com.ee.assets.models.AssetsInfo
import com.ee.assets.transformers.{ExternalHosts, PathElement}


trait Deployer {

  /** deploy the file to some location
    * @param filename name of file
    * @param lastModified the last modified date of the file - useful for supporting caching
    * @param contents the file contents - called by name
    * @return the path to the deployed file
    */
  def deploy(filename: String,  lastModified: Long, contents: => InputStream, info : ContentInfo): Either[String,String]

  /**
   * use external location if required
   * @param pathElement
   * @param info
   * @return
   */
  def resolve(pathElement: PathElement, info: AssetsInfo) = {
    PathElement(ExternalHosts.external(pathElement.path))
  }
}

case class ContentInfo(contentType:String, contentEncoding:Option[String] = None)

