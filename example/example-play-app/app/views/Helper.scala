package views

import play.api.Play

object Helper{
  val loader = new com.ee.assets.Loader(None, Play.current.mode, Play.current.configuration)
}