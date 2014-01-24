package com.ee.utils

import java.io.File
import org.specs2.mutable.Specification
import java.net.URL


class fileTest extends Specification {

  import com.ee.utils.file._


  "name and suffix" should {
    "return the name and suffix as a tuple" in {

      nameAndSuffix("file.txt") ===("file", "txt")
      nameAndSuffix("thing.file.txt") ===("thing.file", "txt")
      nameAndSuffix(".gitignore") ===(".gitignore", "")
      nameAndSuffix("some_bad_file_name.") ===("some_bad_file_name", "")
      nameAndSuffix("some_bad_file_name...") ===("some_bad_file_name..", "")
    }
  }

  "distinct files" should {

    def toFiles(files: List[String]): List[File] = {
      files.map(new File(_))
    }

    def relativize(files: List[File]): List[String] = {
      val root = new File(".").getAbsolutePath
      files.map(_.getCanonicalPath.replace(root.substring(0, root.length() - 1), ""))
    }

    "list only distinct files" in {
      val root = "test/public/com/ee/utils/file"
      val paths = List(root + "/testOne", root + "/testOne/one.js", root + "/one.js")
      val out = distinctFiles(toFiles(paths): _*)

      //converting a string to a File makes the path os-dependent, that is why we do it on both sides
      toFiles(relativize(out)) === toFiles(List(root + "/testOne/one.js", root + "/one.js"))
    }


  }

  "common root folder" should {

    "empty for empty args" in commonRootFolder() === ""

    "return parent for single query" in commonRootFolder("a/b") == "a/b"

    "a/b" in commonRootFolder("a/b/c.txt", "a/b/c/d.txt") === "a/b"

    "empty for no common root" in commonRootFolder("z/b/c.txt", "a/b/c/d.txt") === ""

    "n paths" in {
      commonRootFolder(
        "a/b/c/d/e/f.txt",
        "a/b/c/d.txt",
        "a/b/c/d/e/f/g",
        "a/b/c/d/z/f/g/x.txt"
      ) === "a/b/c"

      commonRootFolder(
        "/a/b/c/d/e/f.txt",
        "/a/b/c/d.txt",
        "/a/b/c/d/e/f/g",
        "/a/b/c/d/z/f/g/x.txt"
      ) === "/a/b/c"
    }

    "leading slash is different to no slash" in
      commonRootFolder(
        "/a/b/c.txt",
        "a/b/c.txt"
      ) === ""
  }

}