package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel._
import bon.jo.app.service.DistantService
import bon.jo.app.{ConfParam, Response, User}
import bon.jo.html.DomShell
import bon.jo.service.Raws._
import bon.jo.view.SiteModelView

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON



class SiteService(implicit val user: User) {

  import imp._

  def console: Any => Unit = DomShell.log


  final def saveItems(l: List[MenuItem]): Future[List[Response]] = {
    console("save Image Start")
    Future.sequence(l.map(menuService.save))
  }

  def saveAll(): Future[List[Response]] = {


    def saveImae(): Future[List[Response]] = {
      console("save Image Start")
      Future.sequence(siteModel.allImages.map(imageService.save))

    }

    def saveOeuvre(): Future[List[Response]] = {
      console("ssaveOeuvre Start")
      Future.sequence(siteModel.allOeuvres map (oeuvreService.save))
    }

    val orderItem = (siteModel.allItem.filter(_.parent.isEmpty) ++ siteModel.allItem.filter(_.parent.isDefined)).toList
    saveImae().flatMap(_ => saveItems(orderItem).flatMap(_ => saveOeuvre()))


  }


  var siteView: SiteModelView = _

  def createNewSubMenuItem(str: String, parentp: MenuItem): MenuItem = {
    createNewMainMenuItem(str).copy(parent = Some(parentp))
  }

  var maxId: Int = 0

  def createNewMainMenuItem(text: String): MenuItem = {
    registerId(MenuItem(maxId + 1, text, "", None, None))
  }


  object ReqBridge {
    val gMenuItem: MenuItem => js.Any = RawsObject.ItemRawExport.apply
    val gOeuvre: Oeuvre => js.Any = RawsObject.OeuvreRawExport.apply
    val gImage: Image => js.Any = RawsObject.ImageRawExport.apply

    def toJsStirng(s : js.Any): String = JSON.stringify(s)
    implicit val trMenuItem: MenuItem => String = gMenuItem.andThen(toJsStirng)
    implicit val trOeuvre: Oeuvre => String = gOeuvre.andThen(toJsStirng)
    implicit val trImage: Image => String = gImage.andThen(toJsStirng)
    // implicit def gReadf[A <: js.Object]: js.Any => A = _.asInstanceOf[A]
    implicit val reversegMenuItem: js.Any => MenuItem = (an: js.Any) => {
      ItemImport(an.asInstanceOf[Raws.ItemRawExport])
    }
    implicit val reversegOeuvre: js.Any => Oeuvre = (an: js.Any) => {
      OeuvreImport(an.asInstanceOf[Raws.OeuvreRawExport])(siteModel.allImages.map(_.toKeyValue).toMap, siteModel.allItem.map(_.toKeyValue).toMap)
    }
    implicit val reversegImage: js.Any => Image = (an: js.Any) => {
      ImageImport(an.asInstanceOf[Raws.ImageRawExport])
    }
  }


  object services {


    import ReqBridge._

    object menuService extends DistantService[MenuItem](ConfParam.apiMenu())

    object oeuvreService extends DistantService[Oeuvre](ConfParam.apiOeuvre())

    object imageService extends DistantService[Image](ConfParam.apiImage())

  }

  val menuService: DistantService[MenuItem] = services.menuService
  val oeuvreService: DistantService[Oeuvre] = services.oeuvreService
  val imageService: DistantService[Image] = services.imageService


  object Legacy {
    val allImages: Map[Int, Image] = RawsObject.images.map(Convs.imageCon).map(keyValue).toMap
    val AllTheme: js.Array[MenuItem] = RawsObject.themes.map(Convs.MenuItemConv).map(registerId)
    val AllOeuvre: js.Array[Oeuvre] = RawsObject.oeuvres.map(e => {
      val ee = Convs.OeuvreConv(e)
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
  }

  val siteModel: SiteModel = SiteModel()

  siteModel.items = Legacy.AllTheme.toList

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
    me.theme.foreach(e => {
      e.oeuvres = e.oeuvres.filter(me != _)
    })
    to.oeuvres = to.oeuvres :+ me
    siteView.contentChange(to)
  }

  def registerId(themes: MenuItem): MenuItem = {
    maxId = Math.max(maxId, themes.id)
    themes
  }

  def keyValue(i: Image): (Int, Image) = i.id -> i


  def export: GlobalExport = RawsObject.GlobalExport(siteModel)

  def OeuvreImport(o: OeuvreRawExport)(implicit imageMap: Map[Int, Image], mMap: Map[Int, MenuItem]): Oeuvre = {
    Oeuvre(o.id, imageMap(o.image), o.name, o.description, Dimension(o.dimension.x, o.dimension.y), o.date, mMap.get(o.theme))
  }

  def ImageImport(imageRaw: ImageRawExport): Image = Image(imageRaw.id, imageRaw.link, imageRaw.base)

  def ItemImport(i: ItemRawExport): MenuItem = {


    val ret = MenuItem(i.id, i.text, i.link, None, None)

    ret
  }

  def link(implicit imageMap: Map[Int, Image], oeuvres: Map[Int, Oeuvre], item: Map[Int, MenuItem], itemRwMap: Map[Int, ItemRawExport]) = {
    item.values.foreach(i => {
      if (!js.isUndefined(itemRwMap(i.id).parent)) {
        i.parent = item.get(itemRwMap(i.id).parent.toString().toInt)
      }
      if (!js.isUndefined(itemRwMap(i.id).image)) {
        i.image = imageMap.get(itemRwMap(i.id).image.toString().toInt)
      }
      itemRwMap(i.id).oeuvres.foreach(oId => {
        i.oeuvres = i.oeuvres :+ oeuvres(oId)
      })
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
    implicit val itemRwMap: Map[Int, ItemRawExport] = `export`.items.map(e => e.id -> e).toMap
    implicit val item: Map[Int, MenuItem] = `export`.items.map(ItemImport).map(_.toKeyValue).toMap
    implicit val imageMap: Map[Int, Image] = `export`.images.map(ImageImport).map(_.toKeyValue).toMap
    implicit val oeuvres: Map[Int, Oeuvre] = `export`.oeuvres.map(OeuvreImport).map(_.toKeyValue).toMap


    link

    newModel.items = item.values.filter(_.parent.isEmpty).toList
    siteModel.items = newModel.items
    refreshId(item.keys)
  }

  object imp {
    implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global
  }

}














