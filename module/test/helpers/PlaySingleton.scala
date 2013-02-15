package helpers

import play.api.Play
import play.api.test.FakeApplication
import scala.Some
import scala.Some

/**
 * Utility to ensure only one instance of FakeApplication is started for tests
 */
object PlaySingleton {
  def start() {
    this.synchronized {
      Play.maybeApplication match {
        case Some(fakeApp) =>
        case None => Play.start(FakeApplication())
      }
    }
  }

  def stop() {
    this.synchronized {
      Play.maybeApplication match {
        case Some(fakeApp) => {
          Play.stop()
        }
        case None =>
      }
    }
  }
}