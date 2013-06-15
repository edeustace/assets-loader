package com.ee.assets.exceptions

class AssetsLoaderException(msg:String,e:Throwable) extends RuntimeException(msg,e){
  def this(msg:String) = this(msg, new Throwable())
}



