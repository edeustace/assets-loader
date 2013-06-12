package com.ee.assets.deployment

import java.io.InputStream


class NullDeployer extends Deployer{
  def deploy(filename: String, lastModified: Long, contents : => InputStream, info : ContentInfo): Either[String,String] = try{
    val bytes = toByteArray(contents)
    Right("/deployed" + filename)
  } catch {
    case e : Throwable => Left(e.getMessage)
  }

  private def toByteArray(is:InputStream) = Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray
}
