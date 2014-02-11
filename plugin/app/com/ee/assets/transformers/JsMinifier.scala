package com.ee.assets.transformers

import com.ee.js.JavascriptCompiler
import com.ee.log.Logger
import com.google.javascript.jscomp.CompilerOptions

object JsMinifier {
  def apply(compilerOptions: Option[CompilerOptions] = None) = new JsMinifier(compilerOptions).run _
}

class JsMinifier(compilerOptions: Option[CompilerOptions] = None) extends Transformer[String, String] {

  val logger = Logger("js-minifier")

  private def minifyJs(contents: String): String = JavascriptCompiler.minify(contents, None, compilerOptions)

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {
    elements.map {
      e =>
        val (name, suffix) = com.ee.utils.file.nameAndSuffix(e.path)
        val nameOut = s"$name.min.$suffix"
        ContentElement(nameOut, minifyJs(e.contents), e.lastModified)
    }
  }
}
