package com.ee.assets

import com.ee.assets.models.AssetsLoaderConfig
import com.ee.assets.transformers.{ContentElement, Element, PathElement}
import org.specs2.mutable.{Before, Specification}

class TransformationBuilderTest extends Specification {


  def read(e: Seq[Element[Unit]]): Seq[Element[String]] = e.map {
    el => ContentElement(s"${el.path}/read", "", None)
  }

  def concat(e: Seq[Element[String]]): Seq[Element[String]] = e.map {
    el => ContentElement(s"${el.path}/concat", "", None)
  }

  def gzip(e: Seq[Element[String]]): Seq[Element[Array[Byte]]] = e.map {
    el => ContentElement(s"${el.path}/gzip", Array[Byte](), None)
  }

  def minify(e: Seq[Element[String]]): Seq[Element[String]] = e.map {
    el => ContentElement(s"${el.path}/minify", "", None)
  }

  def stringWriter(e: Seq[Element[String]]): Seq[Element[Unit]] = e.map {
    el => PathElement(s"${el.path}/string-write")
  }

  def byteWriter(e: Seq[Element[Array[Byte]]]): Seq[Element[Unit]] = e.map {
    el => PathElement(s"${el.path}/byte-write")
  }

  def webPath(e: Seq[Element[Unit]]): Seq[Element[Unit]] = e.map {
    el => PathElement(s"${el.path}/web")
  }

  val builder = new TransformationBuilder(
    read,
    concat,
    gzip,
    minify,
    stringWriter,
    byteWriter,
    webPath
  )

  class conf(config: AssetsLoaderConfig) extends Before {
    lazy val fn = builder.build(config)
    lazy val result = fn(Seq(PathElement("blah")))
    lazy val firstPath = result(0).path

    override def before: Any = {}
  }

  "build" should {

    "nothing" in new conf(AssetsLoaderConfig(false, false, false, false)) {
      firstPath === "blah/web"
    }

    "concat" in new conf(AssetsLoaderConfig(true, false, false, false)) {
      firstPath === "blah/read/concat/string-write"
    }

    "minify" in new conf(AssetsLoaderConfig(false, true, false, false)) {
      firstPath === "blah/read/minify/string-write"
    }

    "gzip" in new conf(AssetsLoaderConfig(false, false, true, false)){
      firstPath === "blah/read/gzip/byte-write"
    }

    "concat + minify" in new conf(AssetsLoaderConfig(true, true, false, false)) {
      firstPath === "blah/read/concat/minify/string-write"
    }

    "concat + minify + gzip" in new conf(AssetsLoaderConfig(true, true, true, false)) {
      firstPath === "blah/read/concat/minify/gzip/byte-write"
    }

    "minify + gzip" in new conf(AssetsLoaderConfig(false, true, true, false)){
      firstPath === "blah/read/minify/gzip/byte-write"
    }

    "concat + gzip" in new conf(AssetsLoaderConfig(true, false, true, false)) {
      firstPath === "blah/read/concat/gzip/byte-write"
    }
  }
}
