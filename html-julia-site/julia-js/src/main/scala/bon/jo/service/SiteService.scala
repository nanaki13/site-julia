package bon.jo.service

import bon.jo.SiteModel._
import bon.jo.app.RequestHttp.POST
import bon.jo.app.service.DistantService
import bon.jo.app.{ConfParam, RequestHttp, Response, User}
import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws.{WithId, _}
import bon.jo.view.SiteModelView
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.FormData

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON


class SiteService(implicit val user: User, val executionContext: ExecutionContext) {
  def root(listImg: FinalComponent[_]) = {
    siteView.root(listImg)
  }

  def hideAll = siteView.hideAll

  def showAll = siteView.displayAll


  def console: Any => Unit = Logger.log


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

    def toJsStirng(s: js.Any): String = JSON.stringify(s)

    implicit val trMenuItem: MenuItem => String = gMenuItem.andThen(toJsStirng)
    implicit val trOeuvre: Oeuvre => String = gOeuvre.andThen(toJsStirng)
    implicit val trImage: Image => String = gImage.andThen(toJsStirng)
    // implicit def gReadf[A <: js.Object]: js.Any => A = _.asInstanceOf[A]
    implicit val reversegMenuItem: js.Any => Raws.ItemRawExport = _.asInstanceOf[Raws.ItemRawExport]
    implicit val reversegOeuvre: js.Any => Raws.OeuvreRawExport = _.asInstanceOf[Raws.OeuvreRawExport]
    implicit val reversegImage: js.Any => Raws.ImageRawExport = _.asInstanceOf[Raws.ImageRawExport]
  }


  trait mId {
    var mid = 0

    def newId: Int = {
      mid += 1
      mid
    }
  }
  class KeepId[C,A <: WithId]( url : String) (implicit read: js.Any =>A, write:C => String, user: User) extends DistantService[C, A](url)  with mId {

    override def getAll: Future[js.Array[A]] = super.getAll.map { e => {
      mid = e.map(_.id).max
      e
    }
    }
  }
  object services {


    import ReqBridge._



    object menuService extends KeepId[MenuItem, ItemRawExport](ConfParam.apiMenu())

    object oeuvreService extends KeepId[Oeuvre, OeuvreRawExport](ConfParam.apiOeuvre())


    object imageService extends KeepId[Image, ImageRawExport](ConfParam.apiImage()) with PostForm


  }

  val menuService :  KeepId[MenuItem, ItemRawExport] = services.menuService
  val oeuvreService: KeepId[Oeuvre, OeuvreRawExport]  = services.oeuvreService
  val imageService: KeepId[Image, ImageRawExport]  with PostForm = services.imageService


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


  def getGlobalExport: Future[GlobalExport] = for {
    img <- imageService.getAll
    th <- menuService.getAll
    oe <- oeuvreService.getAll
  } yield {
    js.Dynamic.literal(
      items = th,
      oeuvres = oe,
      images = img,
    ).asInstanceOf[GlobalExport]
  }


  val siteModel: SiteModel = SiteModel()

  def loadFromOldSite: Future[List[MenuItem]] = Future.successful(Legacy.AllTheme.toList)

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

  def toSiteModelElements(export: GlobalExport): Seq[MenuItem] = {

    implicit val itemRwMap: Map[Int, ItemRawExport] = `export`.items.map(e => e.id -> e).toMap
    implicit val item: Map[Int, MenuItem] = `export`.items.map(ItemImport).map(_.toKeyValue).toMap
    implicit val imageMap: Map[Int, Image] = `export`.images.map(ImageImport).map(_.toKeyValue).toMap
    implicit val oeuvres: Map[Int, Oeuvre] = `export`.oeuvres.map(OeuvreImport).map(_.toKeyValue).toMap

    link
    refreshId(item.keys)
    item.values.filter(_.parent.isEmpty).toList

  }


}

trait PostForm {


  def url: String

  def post(formData: FormData): Future[Response] = {
    new RequestHttp(urlDesr = url, method = POST, json = false).sendBody(formData)
  }

}














