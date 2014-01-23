package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ReadMinifyWriteTest extends Specification with BaseIntegration {


  "read, minifyjs, write" should {

    val outDir = makePath("target", "test-files", "read-minify-write")

    "work" in new cleanGenerated(outDir) {

      val read = new ElementReader(readFn("it"))
      val write = new ElementWriter(writeFn(outDir))
      val minifyJs = new JsMinifier()

      val transformation = new TransformationSequence(read, minifyJs, write)

      transformation.run(
        Seq(
          Element(makePath(pkg, "js-files", "one.js")),
          Element(makePath(pkg, "js-files", "two.js"))
        )
      )

      readFn(outDir)(makePath(pkg, "js-files", "one.min.js")).map {
        c =>
          c === "var x=function(){console.log(\"hello one\")};"
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

        val transformation = new TransformationSequence(read, minifyCss, write)

        transformation.run(
          Seq(
            Element(makePath(pkg, "css-files", "one.css")),
            Element(makePath(pkg, "css-files", "two.css"))
          )
        )

        readFn(outDir)(makePath(pkg, "css-files", "one.min.css")).map {
          c =>
            c === ".test{color:#943;font-family:normal}"
            success
        }.getOrElse(failure("can't find file"))
      }


    }
  }
}
