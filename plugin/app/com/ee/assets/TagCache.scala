package com.ee.assets

import scala.collection.mutable
import play.api.templates.Html

object TagCache {
  val js : mutable.Map[String,Html] = mutable.Map()
  val css : mutable.Map[String,Html] = mutable.Map()
}
