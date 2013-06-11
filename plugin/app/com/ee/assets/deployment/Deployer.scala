package com.ee.assets.deployment

trait Deployer {

  /** deploy the file to some location
    * @param filename name of file
    * @param lastModified the last modified date of the file - useful for supporting caching
    * @param contents the file contents - called by name
    * @return the path to the deployed file
    */
  def deploy(filename: String,  lastModified: Long, contents: => String): Either[String,String]

}

