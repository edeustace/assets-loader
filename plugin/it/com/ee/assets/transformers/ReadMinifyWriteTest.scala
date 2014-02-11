package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ReadMinifyWriteTest extends Specification with BaseIntegration {


  "read, minifyjs, write" should {

    val outDir = makePath("target", "test-files", "read-minify-write")

    "work" in new cleanGenerated(outDir) {

      val read = ElementReader(readFn("it"))
      val write = ElementWriter(writeFn(outDir))
      val minifyJs = JsMinifier()


      val elements = Seq(
        PathElement(makePath(pkg, "js-files", "one.js")),
        PathElement(makePath(pkg, "js-files", "two.js"))
      )

      (read andThen minifyJs andThen write)(elements)

      readFn(outDir)(makePath(pkg, "js-files", "one.min.js")).map {
        e =>
          e.contents === "var x=function(){console.log(\"hello one\")};"
          success
      }.getOrElse(failure("can't find file"))
    }
  }

  "read, minify css, write" should {

    "work" in {
      val outDir = makePath("target", "test-files", "read-minify-write-css")

      "work" in new cleanGenerated(outDir) {

        val read = new ElementReader(readFn("it"))
        val write = new ElementWriter(writeFn(outDir))
        val minifyCss = new CssMinifier()

        val elements = Seq(
          PathElement(makePath(pkg, "css-files", "one.css")),
          PathElement(makePath(pkg, "css-files", "two.css"))
        )

        (read.run _ andThen minifyCss.run _ andThen write.run _)(elements)

        readFn(outDir)(makePath(pkg, "css-files", "one.min.css")).map {
          e =>
            e.contents === ".test{color:#943;font-family:normal}"
            success
        }.getOrElse(failure("can't find file"))
      }


    }
  }
}
