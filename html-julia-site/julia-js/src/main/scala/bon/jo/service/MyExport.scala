package bon.jo.service

import bon.jo.SiteModel.MenuItemExport
import bon.jo.service.Raws.ItemRawExport

import scala.scalajs.js

class MyExport(id: Int, text: String, link: String, image: Option[Int], parent: Option[Int], children: Array[Int], oeuvres: Array[Int]) extends MenuItemExport[ItemRawExport](id, text, link, image, parent, children) {
  override def export(): ItemRawExport = {
    val ret = js.Dynamic.literal(
      id = id,
      text = text,
      link = link,
      children = js.Array(children: _ *),
      oeuvres = js.Array(oeuvres: _ *)
    )
    parent match {
      case Some(value) => {
        ret.parent = value
      }
      case None =>
    }
    image match {
      case Some(value) => ret.image = value
      case None =>
    }
    ret.asInstanceOf[ItemRawExport]
  }
}
