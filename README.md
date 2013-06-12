# assets-loader

A play plugin that concatenates, minifies and gzips your JS or Css.

It does 2 things:

* Allows you to point to a directory of js or css files to load them all
* Processes the files depending on the configuration you provide

## Supported Versions
Play 2.0.4/Scala 2.9.1

### Support for Play 2.1.2/Scala 2.10
There is another branch called 211_version that has support for Play 2.1.2.

It is in beta - but will become the master branch once I've had a chance to use it in a real application.

To use it in Play 2.1.2 add:

    "com.ee" %% "assets-loader" % "0.10-SNAPSHOT"

To your dependencies


## Example

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

You can add these scripts to your template like so:

    <head>
      @com.ee.assets.Loader("javascripts/my-app/controllers", "javascript/my-app/singleFile.js")
    </head>

The loader will concatenate singleFile.js, app.js and helper.js into one file, minify it then gzip it and place it in your target folder and return a script tag so your html will look like this:

    <head>
      <script type="text/javascript" src="/assets/javascripts/my-app/controllers-23423423.min.gz.js"/>
    </head>

## Installing

#### Add the Asset Loader as a dependency to your build:

      val assetsLoader = "com.ee" %% "assets-loader" % "0.6-SNAPSHOT"

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


### Release Notes

### 0.9.3
- Added ability for clients to use the assets processed by Assets Loader for deployment
-- Added a trait Deployer that clients can implement and then instantiate Loader with this Implementation
! This is a breaking change - Loader is no longer an object.

### 0.9.2
- First Version
