package com.ee.assets.transformers

trait Element[A] {
  def path: String

  def contents: A

  def lastModified: Option[Long]

}

abstract class BaseElement[A](val path: String,
                          val contents: A,
                          val lastModified: Option[Long]) extends Element[A]

case class ContentElement[A](val path: String,
                             val contents: A,
                             val lastModified: Option[Long]) extends Element[A]

case class PathElement(override val path: String) extends BaseElement[Unit](path, Unit, None)


trait DeployedElement extends Element[Unit] {

  override def lastModified: Option[Long] = None

  override def contents: Unit = Unit
}

case class SimpleDeployedElement(val path:String) extends DeployedElement
