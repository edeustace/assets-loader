package com.ee.assets

import play.api.Configuration

package object models {

  /**
   * Config for an asset loader
   * @param concatenate
   * @param minify
   * @param gzip
   * @param deploy - if false then the deployer will not be invoked
   */
  case class AssetsLoaderConfig(concatenate:Boolean, minify: Boolean, gzip:Boolean, deploy:Boolean)

  object AssetsLoaderConfig{
    def fromAppConfiguration(mode:String,suffix:String,configuration:Configuration) : AssetsLoaderConfig = {

        val specificConfig : Option[Configuration] = configuration.getConfig("assetsLoader."+mode).map{ modeConfig =>
          modeConfig.getConfig(suffix).getOrElse(modeConfig)
        }

        implicit def toBoolean(property: String): Boolean = {
          specificConfig.map{ c =>
            c.getBoolean(property).getOrElse(false)
          }.getOrElse(false)
        }

        AssetsLoaderConfig("concatenate", "minify", "gzip", "deploy")
    }
  }

  case class AssetsInfo(webPath:String, filePath:String)

}

