package com.ee.assets.models

import org.specs2.mutable.Specification
import play.api.Configuration
import com.typesafe.config.{ConfigFactory, Config}

class AssetsLoaderConfigTest extends Specification{

  def wrap(s:String) : Configuration = {
    val base = ConfigFactory.parseString("{ assetsLoader: " + s + "}")
    new Configuration(base)
  }

  "AssetsLoaderConfig" should {

    "build from app configuration - empty config" in {
      val c = "{}"
      AssetsLoaderConfig.fromAppConfiguration("dev",Suffix.css, wrap(c)) === AssetsLoaderConfig(false, false, false, false)
      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.js, wrap(c)) === AssetsLoaderConfig(false, false, false, false)
    }

    "build from app configuration - no file specific config" in {
      val c =
        """
          |{
          | dev: {
          |  concatenate: true
          |  minify: true
          |  gzip: true
          |  deploy: true
          | }
          |}
        """.stripMargin

      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.css, wrap(c)) === AssetsLoaderConfig(true, true, true, true)
      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.js, wrap(c)) === AssetsLoaderConfig(true, true, true, true)
    }

    "build from app configuration - js only specific config" in {
      val c =
        """
          |{
          | dev: {
          |  js: {
          |   concatenate: true
          |   minify: true
          |   gzip: true
          |  }
          | }
          |}
        """.stripMargin

      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.css, wrap(c)) === AssetsLoaderConfig(false, false, false, false)
      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.js, wrap(c)) === AssetsLoaderConfig(true, true, true, false)
    }

    "build from app configuration - js and css specific config" in {
      val c =
        """
          |{
          | dev: {
          |  js: {
          |   concatenate: true
          |   minify: false
          |   gzip: true
          |   deploy: false
          |  }
          |  css : {
          |    concatenate: false
          |    minify: true
          |    gzip: false
          |    deploy: true
          |  }
          | }
          |}
        """.stripMargin

      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.css, wrap(c)) === AssetsLoaderConfig(false, true, false, true)
      AssetsLoaderConfig.fromAppConfiguration("dev", Suffix.js, wrap(c)) === AssetsLoaderConfig(true, false, true, false)
    }
  }

}
