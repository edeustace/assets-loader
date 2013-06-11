package com.ee.assets.deployment


class NullDeployer extends Deployer{
  def deploy(filename: String, lastModified: Long, contents : => String): Either[String,String] = Right("/deployed" + filename)
}
