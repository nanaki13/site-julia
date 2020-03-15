package bon.jo.service

import bon.jo.SiteModel
import bon.jo.SiteModel.{Image, MenuItem, Oeuvre}
import bon.jo.service.Convs.{MenuItemConv, OeuvreConv, imageCon}
import bon.jo.service.Raws.{images, oeuvres, themes}
import bon.jo.view.SiteModelView

import scala.scalajs.js


class SiteService {
  def move(me: MenuItem, to: MenuItem)(implicit siteModelView : SiteModelView): Unit = {
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
  val AllTheme: js.Array[MenuItem] = themes.map(MenuItemConv)

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

  val siteModel = SiteModel()
  siteModel.items = AllTheme.toList

}

trait WithService {
  implicit val siteService: SiteService
}