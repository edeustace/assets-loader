package com.ee.assets.transformers

import play.api.test.{PlaySpecification, FakeApplication}
import play.api.Play
import java.io.File

class PlayResourceReaderTest extends PlaySpecification {

  sequential

  "play resource reader" should {
    "reads resource" in {
      running(new FakeApplication()) {
        import play.api.Play.current
        val reader = new PlayResourceReader()
        val path = "public/one/testfile.js"
        val elements = Seq(Element[String](path))
        val read = reader.run(elements)
        read(0).path === path
        Play.resource(read(0).path).map {
          p =>
            val lm = new File(p.getPath)
            read(0).lastModified === Some(lm.lastModified)
            success("ok")
        }.getOrElse(failure("failed to find file"))
      }
    }
  }

}
