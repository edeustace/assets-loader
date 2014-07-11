package com.ee.assets.transformers

import com.ee.assets.models.{SimpleAssetsInfo, Suffix, AssetsLoaderConfig}
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import play.api.test.FakeApplication
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

object Data {
  val hosts = List("http://localhost:9000/", "http://localhost1:9000/", "http://localhost2:9000/", "http://localhost3:9000/")
}

class FileToWebPathTest extends PlaySpecification with Mockito {


  val js = List("/test.js", "test.js")


  val sameHost = SimpleAssetsInfo("assets", "public")
  val externalHost = SimpleAssetsInfo("assets", "public", true)
  
  "External Host Config" should {

    "return out if it starts with /" in new TestConfiguration  {
        val service = new FileToWebPath(sameHost)
        service.toWebPath(js(0)) must beEqualTo(js(0))
    }

    "prepend / out if out starts not with /" in new TestConfiguration {
        val service = new FileToWebPath(sameHost)
        service.toWebPath(js(1)) must beEqualTo(js(0))
    }
    
    "load the host from config correctly" in new TestConfiguration {
      val service = new FileToWebPath(externalHost)
      ExternalHosts.hosts.size must beEqualTo(4)
    }

    "iterate through the hosts if external host is required" in new TestConfiguration {
      val service = new FileToWebPath(externalHost)
      val file = js(1)
      service.toWebPath(file) must beEqualTo(s"${Data.hosts(0)}$file")
      service.toWebPath(file) must beEqualTo(s"${Data.hosts(1)}$file")
      service.toWebPath(file) must beEqualTo(s"${Data.hosts(2)}$file")
      service.toWebPath(file) must beEqualTo(s"${Data.hosts(3)}$file")
      service.toWebPath(file) must beEqualTo(s"${Data.hosts(0)}$file")
    }
  }
}

abstract class EmptyTestConfiguration extends WithApplication (
  FakeApplication(
    additionalConfiguration = Map()))

abstract class TestConfiguration extends WithApplication (
  FakeApplication(
    additionalConfiguration = Map(
      "assetsLoader" -> Map(
        "hosts" -> Data.hosts
      )
    )
  )
)
