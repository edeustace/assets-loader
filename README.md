# assets-loader

A play plugin that concatenates, minifies and gzips your JS or Css.

It will process the files depending on the configuration you provide

## Supported Versions

* Play 2.1.*/Scala 2.10.1 - version: 0.10.*

### Running the examples

The example is a Play 2.1.4 application.

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

    //Loader(deployer:Option[Deployer] = None, mode : Mode.Mode, config : Configuration, closureCompilerOptions : Option[CompilerOptions] = None)
    val loader = new com.ee.assets.Loader(None, Play.current.mode, Play.current.configuration)

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


### Closure compiler options

You can pass in your own closure compiler options when you are instantiating the Loader instance. If you pass in nothing it'll use the default settings.

## Installing

#### Add the Asset Loader as a dependency to your build:

      val assetsLoader = "com.ee" %% "assets-loader" % "0.10.4"
      val assetsLoader = "com.ee" %% "assets-loader" % "0.10.5-SNAPSHOT"

      val assetsLoaderReleases = "ed eustace" at "http://edeustace.com/repository/releases"
      val assetsLoaderSnapshots = "ed eustace" at "http://edeustace.com/repository/snapshots"

      PlayProject(...).settings(
        libraryDependencies += assetsLoader,
        resolvers += assetLoaderReleases
      )


#### Add a configuration for the Loader (either in the main conf or in a separate file that is included)

    assetsLoader: {
      # if within dev/test/prod there is a js/css node - these settings will be used specifically for those files.
      dev : {
        js : {
          concatenate:true
          minify:false
          gzip:false
        }
        css : {
          concatenate:true
          minify:false
          gzip:false
        }
      }
      # if no js/css node defined - settings apply to both
      test : {
        concatenate: true
        minify: false
      }

      prod : {
        concatenate: true
        minify: true
        gzip: true
        # Required if running in prod mode: specify a jar from which to extract the 'public' assets folder.
        # jarfile: "target/staged/my-app.jar"
      }
    }

#### Use the Asset Loader Assets Controller
The default Assets controller in Play doesn't work with the loader because it only does a look up on the classLoader, the provided controller also looks up using the file system.

    GET     /assets/*file               com.ee.assets.controllers.Assets.at(path="/public", file)

#### Logging
If you want to see logs from asset loader - make sure you add a logger for 'assets-loader' to your log config.

### Running production mode

If you have a created a distributable app using `play dist`, the asset loader will to the following:

If you have configure the name of the jarfile in the conf - it'll try and find that jar in the lib folder of your distribution. If it hasn't been configured it'll try to find the application jar file. It presumes that the application jar file is the last jar added to the classpath in the `start` script in the dist folder.


### Developing
Clone the project and run `play`.


### Release Notes

### 0.10.4 
- Fix more issues with running in prod mode.

### 0.10.1
- Enable assets loader to run when app is created using `play dist`
- Created ability to specify separate js and/or css configs per mode

### 0.10-SNAPSHOT
- Play 2.1.X support

### 0.9.4
- Added the option to configure js/css specific settings
- Added the config property 'deploy' to disable deployment for js/css specifically

### 0.9.3
- Added ability for clients to use the assets processed by Assets Loader for deployment
-- Added a trait Deployer that clients can implement and then instantiate Loader with this Implementation
! This is a breaking change - Loader is no longer an object.

### 0.9.2
- First Version
