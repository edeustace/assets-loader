package com.ee.assets

object Templates {


  def script(src: String) = s"""<script type="text/javascript" src="${src}"></script>"""

  def css(src: String) = s"""<link rel="stylesheet" type="text/css" href="${src}"/>"""

  def mainTemplate(files: String, content: String) =
    s"""<!-- Asset Loader -->
        |    <!--
        |    files:
        |    ${files}
        |    -->
        |    ${content}
        |<!-- End -->
      """.stripMargin
}
