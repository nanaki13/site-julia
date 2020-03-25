package bon.jo.service

import java.util.Base64

import bon.jo.SiteModel
import bon.jo.SiteModel._
import bon.jo.app.{ConfParam, JuliaConf, User}
import bon.jo.html.DomShell
import bon.jo.service.Raws._
import bon.jo.view.SiteModelView

import scala.annotation.tailrec
import scala.scalajs.js

object ReadToken {
  def readToken(token : String) :String = {
    val  header = token.substring(0,token.indexOf('.'))
    val data : String = token.substring(token.indexOf('.')+1,token.lastIndexOf('.'))
   val sgn : String   = token.substring(token.lastIndexOf('.')+1,token.length)
 // println  (header,data ,sgn  )
    val h = new String( Base64.getUrlDecoder.decode(header))
    val dat = new String( Base64.getUrlDecoder.decode(data))
    val sg =new String( Base64.getUrlDecoder.decode(sgn))
    println(h  )
    println(dat  )
    println(sg  )
    dat
  }
  println(readToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.cThIIoDvwdueQB468K5xDc5633seEFoqwxjF_xSJyQQ"))
}

class SiteService(val user : User) {

  def console: Any => Unit = DomShell.log


  final def saveItems(l: List[MenuItem])(i: Int)(after: => Unit): Unit = {
    if (i == l.size) {
      after
    } else {
      saveWithReturn(l(i))(r => {
        console("save : " + r + " OK")
        saveItems(l)(i + 1)(after)
      })
    }

  }

  def saveAll(): Any = {
    val allImg = siteModel.allImages
    var imgCnt = allImg.size

    def saveImae(after: => Unit): Unit = {
      console("save Image Start")
      siteModel.allImages.map(saveWithReturn)

        .foreach {
          doUpdate => {
            doUpdate { (retur: Image) =>
              imgCnt = imgCnt - 1
              if (imgCnt == 0) {
                after
              }
              console("save : " + retur + " OK")
            }
          }

        }
    }

    def saveOeuvre(): Unit = {
      console("ssaveOeuvre Start")
      siteModel.allOeuvres map (saveWithReturn) foreach {
        doUpdate => {
          doUpdate((retur: Oeuvre) =>
            console("save : " + retur + " OK"))
        }
      }
    }

    val orderItem = (siteModel.allItem.filter(_.parent.isEmpty) ++ siteModel.allItem.filter(_.parent.isDefined)).toList
    saveImae {
      {
        if (orderItem.nonEmpty) {
          console("saveItems Start")
          saveItems(orderItem)(0)((saveOeuvre()))
        }
      }
    }


  }

  def saveWithReturn(menuItem: MenuItem): (MenuItem => Unit) => Unit = menuService.save(menuItem)(_)

  def saveWithReturn(menuItem: Image): (Image => Unit) => Unit = imageService.save(menuItem)(_)

  def saveWithReturn(menuItem: Oeuvre): (Oeuvre => Unit) => Unit = oeuvreService.save(menuItem)(_)


  var siteView: SiteModelView = _

  def createNewSubMenuItem(str: String, parentp: MenuItem): MenuItem = {
    createNewMainMenuItem(str).copy(parent = Some(parentp))
  }

  var maxId: Int = 0

  def createNewMainMenuItem(text: String): MenuItem = {
    registerId(MenuItem(maxId + 1, text, "", None, None))
  }


  object ReqBridge {
    implicit val gMenuItem: MenuItem => js.Any = RawsObject.ItemRawExport.apply
    implicit val gOeuvre: Oeuvre => js.Any = RawsObject.OeuvreRawExport.apply
    implicit val gImage: Image => js.Any = RawsObject.ImageRawExport.apply

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

}














