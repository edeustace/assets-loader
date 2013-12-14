package com.ee.js

import com.google.javascript.jscomp.{JSError, Compiler, CompilerOptions, JSSourceFile}
import java.io.File

object JavascriptCompiler {


  def minify(source: String, name: Option[String], compilerOptions: Option[CompilerOptions] = None): String = {

    val compiler = new Compiler()
    val extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}")
    val options = compilerOptions.getOrElse(new CompilerOptions())

    val input = JSSourceFile.fromCode(name.getOrElse("unknown"), source)

    compiler.compile(extern, input, options).success match {
      case true => compiler.toSource()
      case false => {
        val errorFolder = dumpJsAndErrors(source, compiler.getErrors)
        throw new RuntimeException("JS Errors see: " + errorFolder)
      }
    }
  }

  private def dumpJsAndErrors(source: String, errors: Array[JSError]): String = {
    val errorFolderName = ".Assets-Loader--JavascriptCompiler"
    val errorString = errors.map {
      e =>
        "[" + e.lineNumber + "]" + e.description
    }.mkString("\n")
    val errorFolder: File = new File(errorFolderName)
    errorFolder.mkdir()
    com.ee.utils.file.writeToFile(errorFolderName + "/errors.log", errorString)
    com.ee.utils.file.writeToFile(errorFolderName + "/all_errors.js", source)
    errorFolder.getAbsolutePath
  }
}