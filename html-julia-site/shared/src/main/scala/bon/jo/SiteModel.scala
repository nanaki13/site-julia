package bon.jo

import java.util.concurrent.atomic.AtomicInteger

import bon.jo.SiteModel.{MenuItem, Oeuvre, SiteTitle}

import scala.reflect.ClassTag
import scala.scalajs.js.annotation._
import scala.util.Random


object SiteModel {

  trait Id {
    val id: Int
  }

  trait IdProvider extends (() => Int)

  @JSExportTopLevel("ProvidedId")
  class ProvidedId extends IdProvider {
    var _id = new AtomicInteger

    override def apply(): Int = _id.incrementAndGet
  }


  @JSExportAll
  abstract class SiteElement(id: Int) {
    //    override val id: Int = idp()
  }


  @JSExportTopLevel("SiteTitle")
  @JSExportAll
  case class SiteTitle(id: Int, text: String) extends SiteElement(id)

  @JSExportTopLevel("Image")
  @JSExportAll
  case class Image(id: Int, link: String) extends SiteElement(id)

  @JSExportTopLevel("Oeuvre")
  @JSExportAll
  case class Oeuvre(id: Int, image: Image, name: String, val dimension: Dimension, date: Int, theme: Option[MenuItem] = None) extends SiteElement(id)

  /* @JSExportTopLevel("Theme")
   @JSExportAll
   case class Theme(id:Int, name: String) extends SiteElement(id)*/

  abstract case class MenuItemExport[ExportType](id: Int, text: String, link: String, image: Option[Int], parent: Option[Int], children: Array[Int]) {
    def export(): ExportType
  }

  @JSExportTopLevel("OeuvreExport")
  @JSExportAll
  case class OeuvreExport(id: Int, image: Image, name: String, dimension: Dimension, date: Int, theme: Option[Int] = None) extends SiteElement(id)

  @JSExportTopLevel("Dimension")
  @JSExportAll
  case class Dimension(x: Float, y: Float)

  @JSExportTopLevel("MenuItem")
  @JSExportAll
  case class MenuItem(id: Int, text: String, link: String, image: Option[Image], var parent: Option[MenuItem]) extends SiteElement(id) {
    def this(text: String, link: String, parent: Option[MenuItem]) = this(0, text, link, None, parent)

    var items: List[MenuItem] = List[MenuItem]()
    var oeuvres: List[Oeuvre] = List[Oeuvre]()

    def randomOeuvre(size: Int): Unit = {
      oeuvres = SiteModel.randomOeuvre(size).toList
    }

    def flatten[R](implicit c: ClassTag[R]): List[R] = {
      (if (c == ClassTag(classOf[MenuItem])) {
        this :: items
      } else {
        oeuvres
      }).asInstanceOf[List[R]]
    }
  }

  def rs: String = Random.nextString(5)

  def randomOeuvre(size: Int): Seq[Oeuvre] = for (_ <- 0 until size) yield {
    Oeuvre(0, Image(0, rs), rs, Dimension(10, 10), 2020)
  }
}

@JSExportTopLevel("SiteModel")
@JSExportAll
case class SiteModel(title: SiteTitle = SiteTitle(0, "Julia le Corre artiste")) {
  def allOeuvres: List[Oeuvre]  = items.flatMap(_.flatten[Oeuvre])


  var items: List[MenuItem] = List[MenuItem]()

  def add(menuItem: MenuItem): Unit = {
    items = items :+ menuItem
  }

  def allItem: Seq[MenuItem] = items.flatMap(_.flatten[MenuItem])


  def fake(): Unit = {
    val m1 = new MenuItem("Les a", "", None)
    m1.items = List(new MenuItem("Les a1", "", None), new MenuItem("Les a2", "", None), new MenuItem("Les a4", "", None))
    m1.items.foreach(_.randomOeuvre(Random.nextInt(5)))
    val mainMenu = List(
      AcceuilMenuItem,
      m1,
      ContactMenuItem
    )
    items = mainMenu
  }


}

object ContactMenuItem extends MenuItem(-1, "Contact", "/contact", None, None)

object AcceuilMenuItem extends MenuItem(-1, "Acceil", "/contact", None, None)