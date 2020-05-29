package bon.jo.service

import bon.jo.SiteModel._
import bon.jo.app.RequestHttp.{GET, POST}
import bon.jo.app.service.DistantService
import bon.jo.app.{ConfParam, RequestHttp, Response, User}
import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws._
import bon.jo.view.SiteModelView
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.FormData

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSON


class SiteService(implicit val user: User, val executionContext: ExecutionContext) {
  def createImage(e: ImageRawExport): Image = {
    ImageImport(e)
  }

  def root(listImg: FinalComponent[_]) = {
    siteView.root(listImg)
  }

  def hideAll = siteView.hideAll

  def showAll = siteView.displayAll


  def console: Any => Unit = Logger.log


  final def saveItems(l: List[ThemeMenuItem]): Future[List[Response]] = {
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

  def createNewSubMenuItem(text: String, parentp: ThemeMenuItem): ThemeMenuItem = {
    ThemeMenuItem(menuService.newId, text, "", None, Some(parentp))
  }


  def createNewMainMenuItem(text: String): ThemeMenuItem = {

    ThemeMenuItem(menuService.newId, text, "", None, None)
  }


  object ReqBridge {
    val gMenuItem: ThemeMenuItem => js.Any = RawsObject.ItemRawExport.apply
    val gOeuvre: Oeuvre => js.Any = RawsObject.OeuvreRawExport.apply
    val gImage: Image => js.Any = RawsObject.ImageRawExport.apply
    val gText: Text => js.Any = RawsObject.TextExport.apply

    def toJsStirng(s: js.Any): String = JSON.stringify(s)

    implicit val trMenuItem: ThemeMenuItem => String = gMenuItem.andThen(toJsStirng)
    implicit val trOeuvre: Oeuvre => String = gOeuvre.andThen(toJsStirng)
    implicit val trImage: Image => String = gImage.andThen(toJsStirng)
    implicit val trTxt: Text => String = gText.andThen(toJsStirng)
    // implicit def gReadf[A <: js.Object]: js.Any => A = _.asInstanceOf[A]
    implicit val reversegMenuItem: js.Any => Raws.ItemRawExport = _.asInstanceOf[Raws.ItemRawExport]
    implicit val reversegOeuvre: js.Any => Raws.OeuvreRawExport = _.asInstanceOf[Raws.OeuvreRawExport]
    implicit val reversegImage: js.Any => Raws.ImageRawExport = _.asInstanceOf[Raws.ImageRawExport]
    implicit val reversegTxt: js.Any => Raws.TextExport = _.asInstanceOf[Raws.TextExport]

  }


  object services {


    def incId(id: Int): Int = id + 1

    implicit val readIDItem : ItemRawExport => Int = _.id
    implicit val readIDOeuvre : OeuvreRawExport => Int = _.id
    implicit val readIDImae : ImageRawExport => Int = _.id
    implicit val readIDText : TextExport => Raws.TextId = _.id
    implicit val idToStr : Raws.TextId => List[String] = e => List(e.uid,e.index.toString)
    implicit val id2ToStr : Int => List[String] = e => e.toString :: Nil
    import ReqBridge._


    object menuService extends KeepId[ThemeMenuItem, ItemRawExport, Int](ConfParam.apiMenu()) {
      override def incId(ID: Int): Int = services.incId(ID)
    }

    object oeuvreService extends KeepId[Oeuvre, OeuvreRawExport, Int](ConfParam.apiOeuvre()) {
      override def incId(ID: Int): Int = services.incId(ID)
    }


    object imageService extends KeepId[Image, ImageRawExport, Int](ConfParam.apiImage()) with PostForm {
      override implicit val ex: ExecutionContext = executionContext

      override def incId(ID: Int): Int = services.incId(ID)
    }

    implicit val orderTextId: Ordering[Raws.TextId] = Ordering.by[Raws.TextId, String](_.uid).orElseBy(_.index)

    object textService extends TextService {
      override def incId(ID: Raws.TextId): Raws.TextId = throw new IllegalStateException("cant increment text id")
    }


  }

  val menuService: KeepId[ThemeMenuItem, ItemRawExport, Int] = services.menuService
  val oeuvreService: KeepId[Oeuvre, OeuvreRawExport, Int] = services.oeuvreService
  val imageService: KeepId[Image, ImageRawExport, Int] with PostForm = services.imageService
  val textService: TextService = services.textService

  object Legacy {
    var maxId = 0

    def registerId(themes: ThemeMenuItem): ThemeMenuItem = {
      maxId = Math.max(maxId, themes.id)
      themes
    }

    val allImages: Map[Int, Image] = RawsObject.images.map(Convs.imageCon).map(keyValue).toMap
    val AllTheme: js.Array[ThemeMenuItem] = RawsObject.themes.map(Convs.MenuItemConv).map(registerId)
    val AllOeuvre: js.Array[Oeuvre] = RawsObject.oeuvres.map(e => {
      val ee = Convs.OeuvreConv(e)
      val withImage = if (e.image_key != null) {
        ee.copy(image = Some(allImages(e.image_key.toInt)))
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
      items = js.Array(th.toIndexedSeq: _ *),
      oeuvres = js.Array(oe.toIndexedSeq: _ *),
      images = js.Array(img.toIndexedSeq: _ *),
    ).asInstanceOf[GlobalExport]
  }


  val siteModel: SiteModel = SiteModel()

  def loadFromOldSite: Future[List[ThemeMenuItem]] = Future.successful(Legacy.AllTheme.toList)

  def move(me: ThemeMenuItem, to: ThemeMenuItem): Unit = {
    siteModel.items = siteModel.items.filter(_ != me)
    me.parent match {
      case Some(value) => {
        value.items = value.items.filter(_ != me)
      }
      case None => siteView.mainRemove(me)
    }
    me.parent = Some(to)
    to.items = to.items :+ me
    menuService.update(me)

  }

  def move(me: Oeuvre, to: ThemeMenuItem): Future[Response] = {
    me.theme.foreach(e => {
      e.oeuvres = e.oeuvres.filter(me != _)
    })
    val n = me.copy(theme = Some(to))
    to.oeuvres = to.oeuvres :+ me.copy(theme = Some(to))
    oeuvreService.update(n)
  }


  def keyValue(i: Image): (Int, Image) = i.id -> i


  def export: GlobalExport = RawsObject.GlobalExport(siteModel)

  def OeuvreImport(o: OeuvreRawExport)(implicit imageMap: Map[Int, Image], mMap: Map[Int, ThemeMenuItem]): Oeuvre = {
    Oeuvre(o.id, if (!js.isUndefined(o.image)) Some(imageMap(o.image.asInstanceOf[Int])) else None, o.name, o.description, Dimension(o.dimension.x, o.dimension.y), o.date, mMap.get(o.theme))
  }

  def ImageImport(imageRaw: ImageRawExport): Image = Image(imageRaw.id, imageRaw.link, imageRaw.base)

  def ItemImport(i: ItemRawExport): ThemeMenuItem = {


    val ret = ThemeMenuItem(i.id, i.text, i.link, None, None)

    ret
  }

  def link(implicit imageMap: Map[Int, Image], oeuvres: Map[Int, Oeuvre], item: Map[Int, ThemeMenuItem], itemRwMap: Map[Int, ItemRawExport]) = {
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


  // def refreshId(item: Iterable[Int]): Unit = maxId = item.max

  def toSiteModelElements(export: GlobalExport): Seq[ThemeMenuItem] = {

    implicit val itemRwMap: Map[Int, ItemRawExport] = `export`.items.map(e => e.id -> e).toMap
    implicit val item: Map[Int, ThemeMenuItem] = `export`.items.map(ItemImport).map(_.toKeyValue).toMap
    implicit val imageMap: Map[Int, Image] = `export`.images.map(ImageImport).map(_.toKeyValue).toMap
    implicit val oeuvres: Map[Int, Oeuvre] = `export`.oeuvres.map(OeuvreImport).map(_.toKeyValue).toMap

    link
    // refreshId(item.keys)
    item.values.filter(_.parent.isEmpty).toList

  }


}

trait PostForm {

  implicit val ex: ExecutionContext

  def url: String

  def post(formData: FormData): Future[ImageRawExport] = {
    new RequestHttp(urlDesr = url, method = POST, json = false)
      .sendBody(formData).map(e => {
      e.body[ImageRawExport].get
    })
  }

}


abstract class TextService(implicit read: js.Any => TextExport, write: Text => String, user: User,
                           executionContext: ExecutionContext,tId : TextExport => Raws.TextId,
                           orderId: Ordering[Raws.TextId],idToStr : Raws.TextId => List[String] )
  extends KeepId[Text, TextExport, Raws.TextId](ConfParam.apiText()) {

  def getByUid(str: String): Future[List[TextExport]] = {
    GET.get(s"$url?uid=$str").map(_.body[js.Array[TextExport]].map(_.toList).getOrElse(Nil))
  }
}


trait mId[ID] {

  def incId(ID: ID): ID

  var mid: ID = null.asInstanceOf[ID]

  def newId: ID = {
    mid = incId(mid)
    mid
  }
}

abstract class KeepId[C, A, ID ](url: String)(implicit read: js.Any => A,
                                                       write: C => String,
                                                       readId: A => ID,
                                                       idToSring : ID => List[String],
                                                       user: User,
                                                       orderId: Ordering[ID],
                                                       executionContext: ExecutionContext)
  extends DistantService[C, A, ID](url)
    with mId[ID] {

  val r = read

  override def getAll: Future[js.Array[A]] = super.getAll.map { e => {
    mid = e.map(readId).max
    e
  }
  }
}






