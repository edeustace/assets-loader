package com.ee.utils
import java.io.File
import org.specs2.mutable.Specification


class fileTest extends Specification{
  import com.ee.utils.file._


  "name and suffix" should {
    "return the name and suffix as a tuple" in {

       nameAndSuffix("file.txt") === ("file", "txt")
       nameAndSuffix("thing.file.txt") === ("thing.file", "txt")
       nameAndSuffix(".gitignore") === (".gitignore", "")
       nameAndSuffix("some_bad_file_name.") === ("some_bad_file_name", "")
       nameAndSuffix("some_bad_file_name...") === ("some_bad_file_name..", "")
    }
  }

  "distinct files" should {

    def relativize( files : List[File]) : List[String] = {
       val root = new File("").getAbsolutePath
       files.map( _.getCanonicalPath.replace(root + "/", "") )
    }

    "list only distinct files" in {
      val root = "test/public/com/ee/utils/file"
      val paths = List(root + "/testOne", root + "/testOne/one.js", root + "/one.js")
      val out = distinctFiles(paths.map( new File(_)) : _*)
      relativize(out) === List(  root + "/testOne/one.js", root + "/one.js")
    }
  }
}