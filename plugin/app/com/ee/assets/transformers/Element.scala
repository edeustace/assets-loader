package com.ee.assets.transformers

abstract class Element[A](val path: String,
                              val contents: A,
                              val lastModified: Option[Long])

case class ContentElement[A](override val path: String,
                      override val contents: A,
                      override val lastModified: Option[Long])
  extends Element[A](
    path,
    contents,
    lastModified
  )

case class PathElement(override val path: String) extends Element[Unit](path, Unit, None)
