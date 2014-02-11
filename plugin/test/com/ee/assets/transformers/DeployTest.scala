package com.ee.assets.transformers

import org.specs2.mutable.Specification
import com.ee.assets.deployment.{ContentInfo, Deployer}
import java.io.InputStream

class DeployTest extends Specification{

  "string deploy" should {
    "work" in {

      val mockDeploy = new Deployer {
        override def deploy(
                             filename: String,
                             lastModified: Long,
                             contents: => InputStream,
                             info: ContentInfo): Either[String, String] = Right(s"deployed/$filename")
      }

      val deploy = StringDeploy(mockDeploy)

      val elements = Seq(
        ContentElement( "1.js", "alert('hello');", None),
        ContentElement( "2.js", "alert('hello');", None)
      )
      val result = deploy(elements)
      result(0).path === "deployed/1.js"
      result(1).path === "deployed/2.js"
    }
  }
}
