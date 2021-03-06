package bon.jo.service

import bon.jo.SiteModel.{Dimension, Image, Oeuvre, ThemeMenuItem}
import bon.jo.html.DomShell
import bon.jo.service.Raws.{ImageRaw, OeuvreRaw, ThemeRaw}

import scala.util.{Failure, Success, Try}

object Convs {

  object DimensionConv {
    val reg = """\s*([^\s]+)\s*cm\s*x\s*([^\s]+)\s*cm\s*""".r

    def apply(s: String): Dimension = {
      s match {
        case reg(x, y) => Try(Dimension(x.trim.toFloat, y.trim.toFloat)) match {
          case Success(s) => s
          case Failure(_) => Dimension(x.trim.replace(",", ".").toFloat, y.trim.replace(",", ".").toFloat)
        }
        case _ => DomShell.log("cant parse : " + s); Dimension(0, 0)
      }
    }
  }

  def OeuvreConv(oeuvreRaw: OeuvreRaw): Oeuvre = Oeuvre(oeuvreRaw.id.toInt, null, oeuvreRaw.title,oeuvreRaw.description, DimensionConv(oeuvreRaw.dimension), oeuvreRaw.date.toInt)


  def MenuItemConv(themeRaw: ThemeRaw): ThemeMenuItem = ThemeMenuItem(themeRaw.id.toInt, themeRaw.name, "", None, None)

  def imageCon(imageRaw: ImageRaw) : Image = Image(imageRaw.image_key.toInt,imageRaw.path,"http://julia-le-corre.fr/rsc/")

}
