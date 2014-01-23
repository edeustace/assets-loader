package com.ee.log

import org.slf4j.LoggerFactory

object Logger {

  def apply(name:String = "") = {
    val logName = if(name.isEmpty || name == null) "assets.loader" else s"assets.loader.$name"
    LoggerFactory.getLogger(logName)
  }


}
