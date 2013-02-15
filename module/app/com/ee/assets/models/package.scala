package com.ee.assets

package object models {

  case class AssetsLoaderConfig(concatenate:Boolean, minify: Boolean, gzip:Boolean)

  case class AssetsInfo(webPath:String, filePath:String)

}

