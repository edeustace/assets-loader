# assets-loader

A play plugin that provides a way of rendering assets depending on your environment.

It does 2 things:

* Allows you to point to a directory of js files to load them all
* Processes the files depending on the configuration you provide

## Supported Versions
Play 2.0.4/Scala 2.9.1

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

### Developing
Clone the project and run `play`.