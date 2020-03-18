package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel.{Dimension, Image, MenuItem, MenuItemExport, Oeuvre}
import bon.jo.html.DomShell
import bon.jo.service.Convs.{MenuItemConv, OeuvreConv, imageCon}
import bon.jo.service.Raws.{ImageRaw, images, oeuvres, themes}
import bon.jo.view.SiteModelView

import scala.scalajs.js


class SiteService {
  var siteView: SiteModelView = _

  def createNewSubMenuItem(str: String, parentp: MenuItem): MenuItem = {
    createNewMainMenuItem(str).copy(parent = Some(parentp))
  }

  var maxId: Int = 0

  def createNewMainMenuItem(text: String): MenuItem = {
    registerId(MenuItem(maxId + 1, text, "", None, None))
  }

  def move(me: MenuItem, to: MenuItem): Unit = {
    siteModel.items = siteModel.items.filter(_ != me)
    me.parent match {
      case Some(value) => {
        value.items = value.items.filter(_ != me)
      }
      case None => siteView.mainRemove(me)
    }
    me.parent = Some(to)
    to.items = to.items :+ me
    siteView.contentChange(to)
  }
  def move(me: Oeuvre, to: MenuItem): Unit = {
    me.theme.foreach(e=> {
      e.oeuvres = e.oeuvres.filter(me != _)
    })
    to.oeuvres = to.oeuvres :+ me
    siteView.contentChange(to)
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

  def OeuvreImport(o: OeuvreRawExport)(implicit imageMap: Map[Int, Image]): Oeuvre = {
    Oeuvre(o.id, imageMap(o.image), o.name, Dimension(o.dimension.x, o.dimension.y), o.date, None)
  }

  def ImageImport(imageRaw: ImageRawExport) = Image(imageRaw.id, imageRaw.link)

  def ItemImport(i: ItemRawExport)(implicit imageMap: Map[Int, Image], oeuvres: Map[Int, Oeuvre]): MenuItem = {
    val de = js.isUndefined(i.image)

    val img = if (de) None else imageMap.get(i.image.toString().toInt)
    val ret = MenuItem(i.id, i.text, i.link, img, None)
    i.oeuvres.foreach(oId => {
      ret.oeuvres = ret.oeuvres :+ oeuvres(oId)
    })
    ret
  }

  def link(implicit imageMap: Map[Int, Image], oeuvres: Map[Int, Oeuvre], item: Map[Int, MenuItem], itemRwMap: Map[Int, ItemRawExport]) = {
    item.values.foreach(i => {
      if (!js.isUndefined(itemRwMap(i.id).parent)) {
        i.parent = item.get(itemRwMap(i.id).parent.toString().toInt)
      }
      itemRwMap(i.id).children.foreach(e => {
        val child = item(e)
        i.items = i.items :+ child
        child.parent = Some(i)
      })
    })
  }


  def refreshId(item: Iterable[Int]): Unit = maxId = item.max

  def importSite(export: GlobalExport): Unit = {
    val newModel = SiteModel()

    implicit val imageMap: Map[Int, Image] = `export`.images.map(ImageImport).map(_.toKeyValue).toMap
    implicit val oeuvres: Map[Int, Oeuvre] = `export`.oeuvres.map(OeuvreImport).map(_.toKeyValue).toMap
    implicit val itemRwMap: Map[Int, ItemRawExport] = `export`.items.map(e => e.id -> e).toMap
    implicit val item: Map[Int, MenuItem] = `export`.items.map(ItemImport).map(_.toKeyValue).toMap

    link

    newModel.items = item.values.filter(_.parent.isEmpty).toList
    siteModel.items = newModel.items
    refreshId(item.keys)
  }

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
  val image: Int
  val name: String
  val dimension: DimemsionExport
  val date: Int
  val theme: Int
}

object OeuvreRawExport {
  def apply(oeuvre: Oeuvre): OeuvreRawExport = {

    val ret = js.Dynamic.literal(
      id = oeuvre.id,
      image = oeuvre.image.id,
      name = oeuvre.name,
      dimension = DimemsionExport(oeuvre.dimension),
      date = oeuvre.date,


    )
    oeuvre.theme match {
      case Some(value) => ret.theme = value.id
      case None =>
    }
    ret.asInstanceOf[OeuvreRawExport]
  }
}

@js.native
trait ImageRawExport extends js.Object {
  val id: Int
  val link: String
}

object ImageRawExport {
  def apply(image: Image): ImageRawExport = js.Dynamic.literal(
    id = image.id,
    link = image.link
  ).asInstanceOf[ImageRawExport]
}

@js.native
trait ItemRawExport extends js.Object {
  val id: Int
  val text: String
  val link: String
  val image: js.BigInt
  val parent: js.BigInt
  val children: js.Array[Int]
  val oeuvres: js.Array[Int]
}

@js.native
trait GlobalExport extends js.Object {
  val items: js.Array[ItemRawExport] = js.native
  val oeuvres: js.Array[OeuvreRawExport] = js.native
  val images: js.Array[ImageRawExport] = js.native
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

trait WithService {
  implicit val siteService: SiteService
}