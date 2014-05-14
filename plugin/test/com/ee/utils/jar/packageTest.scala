package com.ee.utils.jar

import com.ee.JarContext
import java.io.File
import java.util.jar.JarFile
import org.specs2.mutable.Specification

class jarPackageTest extends Specification {

  sequential

  "extractJar" should {

    "work" in new JarContext("test/com/ee/utils/jar/jarOne", "target/tmpJarFolder") {
      extractJar( new File(jarPath), destDir, s => true)

      forall(allFiles){ f =>
        println(f)
        new File( s"$outputDirPath/$f").exists === true
      }
    }
  }

  "list children" should {

    "work" in new JarContext("test/com/ee/utils/jar/jarOne", "target/tmpJarFolder") {
      val out = listChildrenInJar(new JarFile(jarPath), p => true).sorted
      out === List("public/", "public/nested/", "public/nested/public.txt", "public/public.txt", "test.txt")
    }

    "filter should work" in new JarContext("test/com/ee/utils/jar/jarOne", "target/tmpJarFolder") {
      def folders(p:String) = p.endsWith("/")
      val noFolders = (folders(_:String) == false)
      val out = listChildrenInJar(new JarFile(jarPath), noFolders).sorted
      out === Seq("public/nested/public.txt", "public/public.txt", "test.txt")
    }
  }

}
