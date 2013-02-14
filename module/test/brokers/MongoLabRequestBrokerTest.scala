package brokers

import org.specs2.mutable.Specification
import db.{JsonCollectionAction, CollectionAction}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.mvc.AnyContent

object FakeLayer extends JsonCollectionAction {
  override def list(collection: String) = "list:" + collection

  def query(collection: String, query: String, fields: String, limit: Int, offset: Int) =
    "query:" + collection + "," + query + "," + fields + "," + limit + "," + offset

  def count(collection: String, query: String) = "count:" + collection + "," + query

  def getOne(collection: String, id: String): String = "getOne:" + collection + "," + id

  def createOne(collection: String, data: String) = "createOne:" + collection + "," + data

  def updateOne(collection: String, id: String, data: String) = "updateOne:" + collection + "," + id + "," + data

  def deleteOne(collection: String, id: String) = "deleteOne:" + collection + "," + id

  def deleteAll(collection: String) = "deleteAll:" + collection
}

class MongoLabRequestBrokerTest extends Specification {

  "handler" should {

    "match GET to list" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val result: Option[String] = handler.handle(FakeRequest("GET", "/mlr/collections/myCollection"))
      result must beSome[String]
      result.get must equalTo("list:myCollection")
    }

    "match GET with query to list" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val params = """f={"fields":1}&l=20&q={"query":1}&sk=0"""

      val url = "/mlr/collections/myCollection?" + params
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """query:myCollection,{"query":1},{"fields":1},20,0""")
    }

    "decode url: GET with query" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val params = "f%3D%7B%22fields%22%3A1%7D%26l%3D20%26q%3D%7B%22query%22%3A1%7D%26sk%3D0"
      val url = "/mlr/collections/myCollection?" + params
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """query:myCollection,{"query":1},{"fields":1},20,0""")
    }

    "match GET with count param to count with Query" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val params = """c=true&f={"fields":1}&l=20&q={"query":1}&sk=0"""
      val url = "/mlr/collections/myCollection?" + params
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """count:myCollection,{"query":1}""")
    }

    "match GET to get one" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val url = "/mlr/collections/myCollection/1"
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """getOne:myCollection,1""")
    }

    "match GET with query to get one" in {
      val url = "/mlr/collections/myCollection/5?apiKey=4fbe6747e4b083e37574238b"
      val handler = new MongoLabRequestBroker(FakeLayer)
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """getOne:myCollection,5""")
    }

    "match GET with hyphen or underscore in collection name" in {

      val url = "/mlr/collections/my-Collection_1/5?apiKey=4fbe6747e4b083e37574238b"
      val handler = new MongoLabRequestBroker(FakeLayer)
      val request = FakeRequest("GET", url)
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """getOne:my-Collection_1,5""")
    }

    "match PUT with id to update one" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val url = "/mlr/collections/myCollection/1"
      val request: FakeRequest[String] = new FakeRequest[String]("PUT", "/mlr/collections/myCollection/1", FakeHeaders(), "body")
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """updateOne:myCollection,1,body""")
    }

    "match POST to create one" in {

      val handler = new MongoLabRequestBroker(FakeLayer)
      val url = "/mlr/collections/myCollection/1"
      val request: FakeRequest[String] = new FakeRequest[String]("POST", "/mlr/collections/myCollection", FakeHeaders(), "body")
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """createOne:myCollection,body""")
    }

    "match DELETE to delete one" in {
      val handler = new MongoLabRequestBroker(FakeLayer)
      val url = "/mlr/collections/myCollection/1"
      val request: FakeRequest[AnyContent] = FakeRequest("DELETE", "/mlr/collections/myCollection/1")
      val result: Option[String] = handler.handle(request)
      result must beSome[String]
      result.get must equalTo( """deleteOne:myCollection,1""")
    }
  }
}
