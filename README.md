# assets-loader

A play plugin that provides a way of rendering assets depending on your environment.

It does 2 things: 

* Allows you to point to a directory of js files to load them all
* Processes the files depending on the configuration you provide

# Example
  
Say you have the following folder structure: 
    

    /public
      /javascripts
        /my-app
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
      @com.ee.assets.Loader("javascripts/my-app/controllers")
    </head>

The loader will concatenate app.js + helper.js into one file, minify it then gzip it and place it in your target folder and return a script tag so your html will look like this: 

    <head>
      <script type="text/javascript" src="/assets/javascripts/my-app/controllers-23423423.min.gz.js"/>
    </head>

    
