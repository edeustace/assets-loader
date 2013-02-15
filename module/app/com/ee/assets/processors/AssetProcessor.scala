package com.ee.assets

import com.ee.assets.models._

trait AssetProcessor {
  /**
   * Store the javascript somewhere
   * @return the url/path to the stored file
   */
  def process(path:String) : String 
  
}