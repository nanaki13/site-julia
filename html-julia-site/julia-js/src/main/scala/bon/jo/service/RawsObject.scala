package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel.{Image, Oeuvre, Text, ThemeMenuItem}
import bon.jo.service.Raws._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object RawsObject {

  object ImageRaw {
    def apply(i: Image): ImageRaw = js.Dynamic.literal(

      path = i.link,
      image_key = i.link
    ).asInstanceOf[ImageRaw]
  }


  object TextExport  {
    def apply(text: Text): TextExport = js.Dynamic.literal(
        id = js.Dynamic.literal(
        uid = text.id.uid,
      index = text.id.index),
      text = text.text

    ).asInstanceOf[TextExport]
  }
  object ImageRawExport {
    def apply(image: Image): ImageRawExport = js.Dynamic.literal(
      id = image.id,
      link = image.link,
      base = image.base
    ).asInstanceOf[ImageRawExport]
  }

  object ItemRawExport {


    def apply(m: ThemeMenuItem): ItemRawExport = {
      val ret = js.Dynamic.literal(
        id = m.id,
        text = m.text,
        link = m.link,

        children = js.Array(m.items.map(_.id): _ *),
        oeuvres = js.Array(m.oeuvres.map(_.id): _ *)
      )
      m.parent.foreach(e => {
        ret.parent = e.id
      })
      m.image.foreach(im=>{
        ret.image = im.id
      })
      ret.asInstanceOf[ItemRawExport]
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

  object OeuvreRawExport {
    def apply(oeuvre: Oeuvre): OeuvreRawExport = {

      val ret = js.Dynamic.literal(
        id = oeuvre.id,

        name = oeuvre.name,
        dimension = DimemsionExport(oeuvre.dimension),
        date = oeuvre.date,
        description = oeuvre.description


      )
      oeuvre.image.foreach(e=> ret.image = e.id)
      oeuvre.theme match {
        case Some(value) => ret.theme = value.id
        case None =>
      }
      ret.asInstanceOf[OeuvreRawExport]
    }
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
