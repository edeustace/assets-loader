package com.ee.assets.processors

import java.io.File

trait AssetProcessor {

  /** Process the list of js files according to the asset loading properties
   */
  def process(jsFiles : List[File]) : List[String]

}