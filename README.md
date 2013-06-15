# assets-loader

A play plugin that concatenates, minifies and gzips your JS or Css.

It will process the files depending on the configuration you provide

## Supported Versions
Play 2.1.2/Scala 2.10.1

* Play 2.0.4/Scala 2.9.1 - use version 0.9.3-SNAPSHOT


### Running the examples

The example is configured as a Play 2.1.2 application by default.

To run the 2.0.4 example:

    cd example
    ./create-example-2.0.4
    cd example-play-app
    play run

To restore the 2.1.2 application:

    cd example
    ./create-example-2.1.2
    cd example-play-app
    play run


## Description

Say you have the following folder structure:


    /public
      /javascripts
        /my-app
          singleFile.js
          /controllers
            app.js
            /subfolder
              /helper.js

A configuration like so:

    dev {
      concatenate: true
      minify: true
      gzip: true
    }

You call the loader:

      val loader = new com.ee.assets.Loader()
      loader.scripts("name")("javascripts/my-app/controllers", "javascript/my-app/singleFile.js")

The loader will concatenate singleFile.js, app.js and helper.js into one file, minify it then gzip it and place it in your target folder and return a script tag so your html will look like this:

      <script type="text/javascript" src="/assets/javascripts/my-app/controllers-23423423.min.gz.js"/>


### Api

    val loader = new com.ee.assets.Loader()
    loader.scripts("name")("path_to_scripts_folder_or_file", ...)
    loader.css("name")("path_to_css_folder_or_file", ...)

Note: The paths argument supports either directories or individual files. If a directory it'll recursively pick the js/css files for you.

### Deployer

When instantiating the loader you can optionally pass in an implementation of the Deployer trait. This looks like this:

    trait Deployer {
      def deploy(filename: String,  lastModified: Long, contents: => InputStream, info : ContentInfo): Either[String,String]
    }

    case class ContentInfo(contentType:String, contentEncoding:Option[String] = None)

This allows you to for example deploy your assets to Amazon S3, then return the deployed path to the Loader which will the return that script path.

    val loader = new com.ee.assets.Loader(Some(S3Deployer))
    loader.scripts("name")("path_to_scripts_folder_or_file", ...)
    loader.css("name")("path_to_css_folder_or_file", ...)


## Installing

#### Add the Asset Loader as a dependency to your build:

      val assetsLoader = "com.ee" %% "assets-loader" % "0.10.1-SNAPSHOT"

      val assetsLoaderReleases = "ed eustace" at "http://edeustace.com/repository/releases"
      val assetsLoaderSnapshots = "ed eustace" at "http://edeustace.com/repository/snapshots"

      PlayProject(...).settings(
        libraryDependencies += assetsLoader,
        resolvers += assetLoaderReleases
      )


#### Add a configuration for the Loader (either in the main conf or in a separate file that is included)

    assetsLoader: {
      dev : {
        concatenate:true
        minify:false
        gzip:false
      }

      test : {
        concatenate: true
        minify: false
      }

      prod : {
        concatenate: true
        minify: true
        gzip: true
      }
    }

#### Use the Asset Loader Assets Controller
The default Assets controller in Play doesn't work with the loader because it only does a look up on the classLoader, the provided controller also looks up using the file system.

    GET     /assets/*file               com.ee.assets.controllers.Assets.at(path="/public", file)

#### Logging
If you want to see logs from asset loader - make sure you add a logger for 'assets-loader' to your log config.
### Developing
Clone the project and run `play`.
