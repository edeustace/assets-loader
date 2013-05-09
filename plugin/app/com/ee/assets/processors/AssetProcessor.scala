package com.ee.assets.processors

import java.io.File

trait AssetProcessor {

  /** Process the list of js files according to the asset loading properties
    * @param concatenatedPrefix - if the files are to be concatenated - provide a prefix
   */
  def process(concatenatedPrefix:String,jsFiles : List[File]) : List[String]

}