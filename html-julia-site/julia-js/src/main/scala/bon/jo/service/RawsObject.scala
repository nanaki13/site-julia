package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel.{Image, MenuItem}
import bon.jo.service.Raws.{GlobalExport, ImageRaw, ImageRawExport, ItemRawExport, OeuvreRaw, ThemeRaw}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object RawsObject {
  object ImageRaw {
    def apply(i: Image): ImageRaw = js.Dynamic.literal(

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

  object ImageRawExport {
    def apply(image: Image): ImageRawExport = js.Dynamic.literal(
      id = image.id,
      link = image.link
    ).asInstanceOf[ImageRawExport]
  }
  object ItemRawExport {
    def apply(m: MenuItem): ItemRawExport = {
      js.Dynamic.literal(
        id = m.id,
        text = m.text,
        link = m.link,
        children = js.Array(m.items.map(_.id): _ *),
        oeuvres = js.Array(m.oeuvres.map(_.id): _ *)
      ).asInstanceOf[ItemRawExport]
    }
  }
  object GlobalExport {
    def apply(siteModel: SiteModel): GlobalExport = {
      val l: List[ItemRawExport] = siteModel.allItem.map(e => {
        new MyExport(e.id, e.text, e.link, e.image.map(_.id), None, e.items.map(_.id).toArray, e.oeuvres.map(_.id).toArray).`export`()
      }).toList
      val o = siteModel.allOeuvres.map(OeuvreRawExport.apply)
      val image = siteModel.allImages.map(ImageRawExport.apply)
      val ret = js.Dynamic.literal(
        items = js.Array(l: _ *),
        oeuvres = js.Array(o: _ *),
        images = js.Array(image: _ *)
      )
      ret.asInstanceOf[GlobalExport]
    }
  }
}
