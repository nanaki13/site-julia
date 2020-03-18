package bon.jo.service

import bon.jo.SiteModel.Image

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object Raws {

  @js.native
  trait OeuvreRaw extends js.Object {
    val date: String
    val description: String
    val dimension: String
    val enable: String
    val id: String
    val image_key: String
    val tech_code: String
    val theme_key: String
    val title: String
  }

  @js.native
  trait ThemeRaw extends js.Object {
    val id: String
    val name: String
  }


  @js.native
  trait ImageRaw extends js.Object {
    val height: String
    val image_key: String
    val path: String
    val width: String

  }
  object ImageRaw{
    def apply(i : Image): ImageRaw = js.Dynamic.literal(

      path = i.link,
      image_key = i.link
    ).asInstanceOf[ImageRaw]
  }

  @JSGlobal("oeuvres")
  @js.native
  object oeuvres extends js.Array[OeuvreRaw]

  @JSGlobal("themes")
  @js.native
  object themes extends js.Array[ThemeRaw]

  @JSGlobal("image_path")
  @js.native
  object images extends js.Array[ImageRaw]


}


