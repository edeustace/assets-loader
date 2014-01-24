package com.ee.assets

import play.api.test._
import play.api.test.TestServer
import play.api.test.FakeApplication
import play.api.mvc.Results
import org.slf4j.LoggerFactory
import org.specs2.specification.{Step, Fragments}

trait ServerSpec {

  implicit val app: FakeApplication = FakeApplication()

  implicit def port: Port = Helpers.testServerPort

  val server = TestServer(port, app)
}

class IntegrationSpecification extends PlaySpecification with Results with ServerSpec {

  sequential

  protected def logger: org.slf4j.Logger = LoggerFactory.getLogger("it.spec")

  override def map(fs: => Fragments) = Step(server.start()) ^ fs ^ Step(server.stop)
}
