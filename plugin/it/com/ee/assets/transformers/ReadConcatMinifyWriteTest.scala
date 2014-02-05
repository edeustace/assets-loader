package com.ee.assets.transformers

import org.specs2.mutable.Specification

class ReadConcatMinifyWriteTest extends Specification with BaseIntegration{


  "Read,Concat,Minify,Write" should {

    val outDir = makePath("target", "test-files", "rcmw-files")
    val fileConcatted = "concatenated.js"
    val fileConcattestMinified = "concatenated.min.js"

    "work" in new cleanGenerated(outDir){

      val read = new ElementReader(readFn("it"))

      val concat = new Concatenator(new PathNamer {
        override def name[A](elements: Seq[Element[A]]): String = fileConcatted
      })

      val minify = new JsMinifier()

      val write = new ElementWriter(writeFn(outDir))

      val sequence = new TransformationSequence(read, concat, minify, write)

      val elements : Seq[Element[String]]= Seq(
        Element[String](makePath(pkg, "js-files", "one.js")),
        Element[String](makePath(pkg, "js-files", "two.js"))
      )

      sequence.run(elements)

      readFn(outDir)(fileConcattestMinified).map {
        c =>
          c === "var x=function(){console.log(\"hello one\")};var y=function(){console.log(\"hello two\")};"
          success
      }.getOrElse(failure("can't find file"))

    }
  }
}
