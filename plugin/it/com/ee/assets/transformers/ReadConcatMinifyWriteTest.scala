package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ReadConcatMinifyWriteTest extends Specification with BaseIntegration{


  "Read,Concat,Minify,Write" should {

    val outDir = makePath("target", "test-files", "rcmw-files")
    val fileConcatted = "concatenated.js"
    val fileConcattestMinified = "concatenated.min.js"

    "work" in new cleanGenerated(outDir){

      val read = ElementReader(readFn("it"))

      val concat = Concatenator(new PathNamer {
        override def name[A](elements: Seq[Element[A]]): String = fileConcatted
      })

      val minify = JsMinifier()

      val write = ElementWriter(writeFn(outDir))

      val combi = read andThen concat andThen minify andThen write

      val elements : Seq[Element[Unit]]= Seq(
        PathElement(makePath(pkg, "js-files", "one.js")),
        PathElement(makePath(pkg, "js-files", "two.js"))
      )

      combi(elements)

      readFn(outDir)(fileConcattestMinified).map {
        e =>
          e.contents === "var x=function(){console.log(\"hello one\")};var y=function(){console.log(\"hello two\")};"
          success
      }.getOrElse(failure("can't find file"))

    }
  }
}
