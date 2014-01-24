package views

import play.api.Play
import java.io.File

object Helper{
  //val loader = new com.ee.assets.Loader(None, Play.current.mode, Play.current.configuration)


  def listFiles(p:String) : String = {

    import play.api.Play.current

    Play.resource(p).map{ url => 

        //val f = new File(url.getFile)
        url.getFile + " " + url.getProtocol 

      }.getOrElse("?")
  }
}