package com.ee.assets.transformers

import com.ee.assets.deployment.{ContentInfo, Deployer}
import com.ee.log.Logger
import java.io.{InputStream, ByteArrayInputStream}

class Deploy(d: Deployer) extends Transformer[String, String] {

  val logger = Logger("deploy")

  override def run(elements: Seq[Element[String]]): Seq[Element[String]] = {

    val deployed: Seq[Element[String]] = {
      for {
        e <- elements
        c <- e.contents
      } yield {
        def contentType = if (e.path.endsWith(".js")) "text/javascript" else "text/css"
        def encoding = if (e.path.contains(".gz")) Some("gzip") else None

        //TODO: is lastmodified still useful and what does it mean in this context.
        val lastModified = 0
        val is: InputStream = new ByteArrayInputStream(c.getBytes())
        d.deploy(e.path, lastModified, is, ContentInfo(contentType, encoding)) match {
          case Right(p) => Some(Element[String](p, None))
          case Left(err) => {
            logger.warn(s"Error deploying: ${e.path}")
            None
          }
        }
      }
    }.flatten

    deployed
  }
}
