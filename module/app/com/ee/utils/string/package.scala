package com.ee.utils

package object string {

  import util.matching.Regex

  val DefaultRegex = """\$\{([^}]+)\}""".r

  /** Interpolate a string
    * @param text - a string that contains tokens to replace
    * @param args - a variable parameter list of either (String,String) or (String,String,String)
    *             If the arg is (String,String) - this is read as key,value
    *             If the arg is (String,String,String) - this is read as key,value,default
    * @return - the interpolated string
    */
  def interpolate(text: String, args: Product*): String = {

    /** Extract a value from a Product
      * Assumes the product is a Tuple2/Tuple3 of type string
      */
    def value(p: Product, default: String): String = p match {
      case (key, value) => if (value != null) value.toString else default
      case (key, value, inlineDefault) => if (value != null) value.toString else inlineDefault.toString
      case _ => throw new RuntimeException(
        "not a valid tuple - must be either (String,String) or (String,String,String)"
      )
    }

    def swap(s: String): String = args.find(_.productElement(0) == s) match {
      case Some(product) => value(product, s)
      case None => s
    }

    interpolateWithLookup(text, swap)
  }

  private def interpolateWithLookup(text: String, lookup: String => String, regex: Regex = DefaultRegex) =
    regex.replaceAllIn(text, (_: scala.util.matching.Regex.Match) match {
      case Regex.Groups(v) => {
        val result = lookup(v)
        result
      }
    })

}