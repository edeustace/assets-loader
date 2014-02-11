package com.ee.assets

import com.ee.assets.models.AssetsLoaderConfig
import com.ee.assets.transformers._

class TransformationBuilder(
                             read: Seq[Element[Unit]] => Seq[Element[String]],
                             concat: Seq[Element[String]] => Seq[Element[String]],
                             gzip: Seq[Element[String]] => Seq[Element[Array[Byte]]],
                             minify: Seq[Element[String]] => Seq[Element[String]],
                             stringWriter: Seq[Element[String]] => Seq[Element[Unit]],
                             byteWriter: Seq[Element[Array[Byte]]] => Seq[Element[Unit]],
                             webPath: Seq[Element[Unit]] => Seq[Element[Unit]]
                             ) {


  def build(config: AssetsLoaderConfig): Seq[Element[Unit]] => Seq[Element[Unit]] = {

    def withStringWrite(writeFn: Seq[Element[String]] => Seq[Element[Unit]]): Seq[Element[Unit]] => Seq[Element[Unit]] = {

      config match {
        case AssetsLoaderConfig(true, false, _, _) => read andThen concat andThen writeFn
        case AssetsLoaderConfig(false, true, _, _) => read andThen minify andThen writeFn
        case AssetsLoaderConfig(true, true, _, _) => read andThen concat andThen minify andThen writeFn
        case AssetsLoaderConfig(false, false, _, _) => read andThen writeFn
      }
    }

    config match {
      case AssetsLoaderConfig(false, false, false, false) => webPath
      case AssetsLoaderConfig(_, _, false, _) => withStringWrite(stringWriter)
      case AssetsLoaderConfig(_, _, true, _ ) => withStringWrite(gzip andThen byteWriter)
      case _ => e => e
    }
  }
}
