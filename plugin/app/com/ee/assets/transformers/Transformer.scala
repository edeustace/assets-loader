package com.ee.assets.transformers


trait Transformer[A,B] {
  def run(elements:Seq[Element[A]]) : Seq[Element[B]]
}


