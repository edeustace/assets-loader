# assets-loader
A play plugin that gives you control over your asset pipeline

# Usage
  
Say you have the following folder structure: 
    

    /public
      /javascripts
        /my-app
          /controllers
            app.js
            /subfolder
              /helper.js

And you call the loader like so: 

    <head>
      @com.ee.assets.Loader("javascripts/my-app/controllers")
    </head>

The loader will concatenate app.js + helper.js into one file, place in your target folder and return a script tag so your html will look like this: 

    <head>
      <script type="text/javascript" src="/assets/javascripts/my-app/controllers-23423423.js"/>
    </head>

    
