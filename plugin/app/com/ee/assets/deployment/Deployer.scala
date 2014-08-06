package com.ee.assets.deployment

import java.io.InputStream

import com.ee.assets.transformers.DeployedElement


trait Deployer {

  /** deploy the file to some location
    * @param filename name of file
    * @param lastModified the last modified date of the file - useful for supporting caching
    * @param contents the file contents - called by name
    * @return the path to the deployed file
    */
  def deploy(filename: String,  lastModified: Long, contents: => InputStream, info : ContentInfo): Either[String,DeployedElement]

}



case class ContentInfo(contentType:String, contentEncoding:Option[String] = None)

