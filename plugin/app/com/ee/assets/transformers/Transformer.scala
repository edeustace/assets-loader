package com.ee.assets.transformers


trait Transformer {
  def run(elements:Seq[Element]) : Seq[Element]
}


