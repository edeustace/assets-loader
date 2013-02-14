import db.ConnectionInitializer
import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.FakeRequest
import scala.Some

object TestConnectionInitializer extends ConnectionInitializer{
  def init(mongoUri:String){}
}

object TestHandlerLookup extends HandlerLookup {
  def getList(collection: String) = Some(new FakeHandler(collection))

  def query(collection: String) = Some(new FakeHandler(collection))

  def getOne(collection: String, id: String) = Some(new FakeHandler(collection, id))

  def createOne(collection: String) = Some(new FakeHandler(collection))

  def updateOne(collection: String, id: String) = Some(new FakeHandler(collection, id))

  def deleteOne(collection: String, id: String) = Some(new FakeHandler(collection, id))

  def deleteAll(collection: String) = Some(new FakeHandler(collection))

  def count(collection:String) = Some(new FakeHandler(collection))
}


case class FakeHandler(collection: String, id: String = "", data: String = "") extends Handler

class MongoRestLayerTest extends Specification {

  def layer = new MongoRestLayer("/mlr", "mongodb://blah", TestHandlerLookup, TestConnectionInitializer)

  "Rest layer" should {

    "match GET all" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("GET", "/mlr/collection"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
    }

    "match GET one item" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("GET", "/mlr/collection/1"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
      handler.get.asInstanceOf[FakeHandler].id must equalTo("1")
    }

    "match DELETE all" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("DELETE", "/mlr/collection"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
    }

    "match DELETE one item" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("DELETE", "/mlr/collection/1"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
      handler.get.asInstanceOf[FakeHandler].id must equalTo("1")
    }

    "match POST query" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("POST", "/mlr/collection"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
    }

    "match POST update" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("POST", "/mlr/collection/1"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
      handler.get.asInstanceOf[FakeHandler].id must equalTo("1")
    }

    "match PUT insert" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("POST", "/mlr/collection"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
    }

    "match POST count" in {
      val handler: Option[Handler] = layer.handlerFor(FakeRequest("POST", "/mlr/collection/count"))
      handler must beSome[Handler]
      handler.get.asInstanceOf[FakeHandler].collection must equalTo("collection")
    }
  }
}
