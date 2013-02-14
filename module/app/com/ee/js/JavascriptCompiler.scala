package com.ee.js

import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile, CompilationLevel }

object JavascriptCompiler {


  def minify(source: String, name: Option[String]): String = {

    val compiler = new Compiler()
    val extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}")
    val options = new CompilerOptions()

    val input = JSSourceFile.fromCode(name.getOrElse("unknown"), source)

    compiler.compile(extern, input, options).success match {
      case true => compiler.toSource()
      case false => {
        val error = compiler.getErrors().head
        com.ee.utils.file.writeToFile("all_error.js", source)
        throw new RuntimeException( error.description )
      }
    }
  }
}