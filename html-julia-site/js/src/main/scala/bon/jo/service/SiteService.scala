package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel.{Dimension, Image, MenuItem, MenuItemExport, Oeuvre}
import bon.jo.html.DomShell
import bon.jo.service.Convs.{MenuItemConv, OeuvreConv, imageCon}
import bon.jo.service.Raws.{images, oeuvres, themes}
import bon.jo.view.SiteModelView

import scala.scalajs.js


class SiteService {
  def createNewSubMenuItem(str: String, parentp: MenuItem): MenuItem = {
    createNewMainMenuItem(str).copy(parent = Some(parentp))
  }

  var maxId: Int = 0

  def createNewMainMenuItem(text: String): MenuItem = {
    registerId(MenuItem(maxId + 1, text, "", None, None))
  }

  def move(me: MenuItem, to: MenuItem)(implicit siteModelView: SiteModelView): Unit = {
    siteModel.items = siteModel.items.filter(_ != me)
    me.parent match {
      case Some(value) => {
        value.items = value.items.filter(_ != me)
      }
      case None => siteModelView.mainRemove(me)
    }
    me.parent = Some(to)
    to.items = to.items :+ me
    siteModelView.contentChange(to)
  }


  def keyValue(i: Image): (Int, Image) = i.id -> i

  val allImages: Map[Int, Image] = images.map(imageCon).map(keyValue).toMap

  def registerId(themes: MenuItem): MenuItem = {
    maxId = Math.max(maxId, themes.id)
    themes
  }

  val AllTheme: js.Array[MenuItem] = themes.map(MenuItemConv).map(registerId)

  val AllOeuvre: js.Array[Oeuvre] = oeuvres.map(e => {
    val ee = OeuvreConv(e)
    val withImage = if (e.image_key != null) {
      ee.copy(image = allImages(e.image_key.toInt))
    } else {
      ee
    }
    val a = if (e.theme_key != null) {
      val th = e.theme_key.toInt
      val withTheme = withImage.copy(theme = AllTheme.find(_.id == th))
      withTheme.theme.get.oeuvres = withTheme.theme.get.oeuvres :+ withTheme
      withTheme
    } else {
      withImage
    }
    a

  })

  val siteModel: SiteModel = SiteModel()
  siteModel.items = AllTheme.toList

  def export: GlobalExport = GlobalExport(siteModel)

}

@js.native
trait DimemsionExport extends js.Object {
  val x: Float
  val y: Float
}

object DimemsionExport {
  def apply(d: Dimension): DimemsionExport = js.Dynamic.literal(x = d.x, y = d.y).asInstanceOf[DimemsionExport]
}

@js.native
trait OeuvreRawExport extends js.Object {
  val id: Int
  val image: Image
  val name: String
  val dimension: Dimension
  val date: Int
  val theme: Int
}

object OeuvreRawExport {
  def apply(oeuvre: Oeuvre): OeuvreRawExport = {
    val th :js.BigInt= oeuvre.theme.map(e => js.BigInt(e.id)).orNull
    val ret = js.Dynamic.literal(
      id = oeuvre.id,
      image = oeuvre.image.id,
      name = oeuvre.name,
      dimension = DimemsionExport(oeuvre.dimension),
      date = oeuvre.date,
      theme = th

    )
    oeuvre.theme match {
      case Some(value) => ret.theme = value.id
      case None => ret.theme = null
    }
    ret.asInstanceOf[OeuvreRawExport]
  }
}

@js.native
trait ItemRawExport extends js.Object {
  val id: Int
  val text: String
  val link: String
  val image: Int
  val parent: Int
  val children: Array[Int]
  val oeuvres: Array[Int]
}

@js.native
trait GlobalExport extends js.Object  {
  val items: Array[ItemRawExport]
  val oeuvres: Array[OeuvreRawExport]
}
  object GlobalExport{
    def apply(siteModel: SiteModel): GlobalExport = {
     val l : List[ItemRawExport] = siteModel.allItem.map(e => {
        new MyExport(e.id, e.text, e.link, e.image.map(_.id), None, e.items.map(_.id).toArray, e.oeuvres.map(_.id).toArray).`export`()
      }).toList
      val o =  siteModel.allOeuvres.map(OeuvreRawExport.apply)

      val ret = js.Dynamic.literal(
        items = js.Array(l: _ *),
        oeuvres = js.Array(o: _ *)
      )
      DomShell.deb()
      ret.asInstanceOf[GlobalExport]
    }
  }

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
      case None => {
        ret.parent = null
      }
    }
    image match {
      case Some(value) => ret.image = value
      case None => ret.image = null
    }
    ret.asInstanceOf[ItemRawExport]
  }
}

trait WithService {
  implicit val siteService: SiteService
}