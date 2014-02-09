package com.ee.assets.transformers

case class Element[A](path:String, contents : Option[A] = None, lastModified : Option[Long] = None)
