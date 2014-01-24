package com.ee.assets

import org.specs2.mutable.Specification
import play.api.Play

class NewLoaderTest extends IntegrationSpecification {


  "Loader" should {

    "read a folder" in {
      logger.info("!!!!!!!!!!!")
      Play.resource("one").map{ url =>
        println(s"url: $url")
        val f = new Fileurl.getFile
        logger.debug(f)
      }.getOrElse{
        println("not found")
      }

      true === true

    }
  }

}
