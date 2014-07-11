# assets-loader

![Build Status](https://www.codeship.io/projects/4218cdb0-bcfd-0131-fa28-4e030f7dcbdf/status)


A play plugin that concatenates, minifies and gzips your JS or Css.

It will process the files depending on the configuration you provide

## Supported Versions

* Play 2.2.*/Scala 2.10.* - version: > 0.11, branch: `master` (this branch no longer supports older play versions)
* Play 2.1.*/Scala 2.9.1 - version: 0.10.x, branch: `play-2.1.x`

### Running the examples

The example is configured as a Play 2.2.1 application by default.

To see the logs in dev mode run:

     play -Dlogger.file=conf/logging/prod.xml

To see the logs in prod mode run:

    ./target/universal/stage/bin/example-221 -Dlogger.file=conf/logging/prod.xml


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

    /**
      Loader(
        deployer:Option[Deployer] = None,
        mode : Mode.Mode,
        config : Configuration,
        closureCompilerOptions : Option[CompilerOptions] = None,
        info : AssetsInfo = AssetsInfo("assets", "public")
      )
    */
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

### Add external / different host(s)

To be able to distribute your assets through one or more different hosts (like vhost or AWS Cloudfront) put a list of hosts in your config like this:

     assetsLoader: {
          ... , 
          hosts: ["http://localhost:9000/", "http://a1.yourhost.com/", "http://cloudfront-abc.com/"]
     }

To deliver your assets in a rotating order from this hosts, pass another parameter to your **SimpleAssetsInfo** like this:

     val loaderExternal = new com.ee.assets.Loader(deployer = deploy, mode = mode, config = config, info = SimpleAssetsInfo("assets", "public", true))

Then just load your assets from that loader like this:

     @views.Helper.loaderExternal.scripts("example-host-a")( "javascripts/example" )
     
See more examples in the example-play-app.     

## Installing

#### Add the Asset Loader as a dependency to your build:

      val assetsLoader = "com.ee" %% "assets-loader" % "0.12.3"

      // snapshot version
      //val assetsLoader = "com.ee" %% "assets-loader" % "0.12.4-SNAPSHOT"

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
          deploy: true
          addHints: true|false (default: false)
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
      }
    }

#### Options

The following options apply to all configurations:

* concatenate (true|*false*) - concatenate the files
* minify (true|*false*) - minify the files
* gzip (true|*false*) - gzip the files
* addHints (true|*false*) - add hint information to the html - useful for debugging


#### Use the Asset Loader Assets Controller
The default Assets controller in Play doesn't work with the loader because it only does a look up on the classLoader, the provided controller also looks up using the file system.

    GET     /assets/*file               com.ee.assets.controllers.Assets.at(path="/public", file)


#### About Css concatenation

Css concatenation doesn't account for paths to other resources within css files, so paths may break if the source css file and the concatenated css are in different folders. We are looking into whats the best way to [solve this](http://github.com/edeustace/assets-loader/issues/24).


#### Logging
If you want to see logs from asset loader - make sure you add a logger for 'assets-loader' to your log config.

### Developing

    git clone git@github.com:edeustace/assets-loader.git
    
#### plugin library

    cd plugin
    play test # unit tests
    play it:test # integration tests
    play clean compile publish-local etc....

#### example app

You need to publish-local in the plugin to update your local repo. Then in the example play app run `play update` to pull in the latest jar. To run the example run `play run`.


### Release Notes

### See releases page from now on...

### 0.11.2
- Always read files as utf-8
- Fixed issues with prod mode + play 2.2.1
- Support in dev mode for windows style paths

### 0.11.1
- Added optional closure CompilerOptions as parameter to the loader

### 0.11.0
- Update for Play 2.2.1

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
