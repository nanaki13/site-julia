package bon.jo

import java.util.concurrent.atomic.AtomicInteger

import bon.jo.SiteModel.{Image, MenuItem, Oeuvre, SiteElement, SiteTitle}

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.scalajs.js.annotation._
import scala.util.Random


object SiteModel {

  case class ImgLinkOb(id: Int, contentType: String, link: String, name: String) extends OkResponse

  object ImgLink {

    def apply(id: Int, contentType: String): String =
      s"/api/image/${id}.${
        contentType.substring(contentType.lastIndexOf('/') + 1)
      }"
  }

  trait OkResponse extends Any


  trait Position {
    def x: Int

    def y: Int
  }

  case class Operation(success: Boolean) extends OkResponse

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
  abstract class SiteElement(val id: Int) {
    implicit def toKeyValue: (Int, this.type) = id -> this


  }


  @JSExportTopLevel("SiteTitle")
  @JSExportAll
  case class SiteTitle(override val id: Int, text: String) extends SiteElement(id)

  @JSExportTopLevel("Image")
  @JSExportAll
  case class Image(override val id: Int, link: String, base : String) extends SiteElement(id)

  object Image {

    trait Ev[R <: SiteElement] extends (R => (Int, R)) {
      override def apply(v1: R): (Int, R) = v1.id -> v1
    }

    implicit object imageConv extends Ev[Image]

  }

  @JSExportTopLevel("Oeuvre")
  @JSExportAll
  case class Oeuvre(override val id: Int, image: Image, name: String, description: String, dimension: Dimension, date: Int, theme: Option[MenuItem] = None) extends SiteElement(id) {
    override def toString: String = s"oeuvre:$id"
  }

  /* @JSExportTopLevel("Theme")
   @JSExportAll
   case class Theme(id:Int, name: String) extends SiteElement(id)*/

  abstract case class MenuItemExport[ExportType](id: Int, text: String, link: String, image: Option[Int], parent: Option[Int], children: Array[Int]) {
    def export(): ExportType
  }

  @JSExportTopLevel("OeuvreExport")
  @JSExportAll
  case class OeuvreExport(override val id: Int, image: Image, name: String, dimension: Dimension, date: Int, theme: Option[Int] = None) extends SiteElement(id)

  @JSExportTopLevel("Dimension")
  @JSExportAll
  case class Dimension(x: Float, y: Float)

  @JSExportTopLevel("MenuItem")
  @JSExportAll
  case class MenuItem(override val id: Int, text: String, link: String, var image: Option[Image], var parent: Option[MenuItem]
                      , var items: List[MenuItem] = List[MenuItem](),
                      var oeuvres: List[Oeuvre] = List[Oeuvre]()) extends SiteElement(id) {
    def this(id: Int) = this(id, "", "", None, None)

    def this(text: String, link: String, parent: Option[MenuItem]) = this(0, text, link, None, parent)


    def randomOeuvre(size: Int): Unit = {
      oeuvres = SiteModel.randomOeuvre(size).toList
    }

    def flatten[R](implicit c: ClassTag[R]): List[R] = {
      (if (c == ClassTag(classOf[MenuItem])) {
        this :: items
      } else if (c == ClassTag(classOf[Image])) {
        val fromItem: List[Option[Image]] = this.items.flatMap(e => e.flatten[Image]).map(e => Some(e))
        val l: List[Option[Image]] = (this.image :: this.oeuvres.map(e => Some(e.image))) ++ fromItem
        l.flatten
      } else {
        oeuvres ++ items.flatMap(e => e.flatten[Oeuvre])
      }).asInstanceOf[List[R]]
    }
  }

  def rs: String = Random.nextString(5)

  def randomOeuvre(size: Int): Seq[Oeuvre] = for (_ <- 0 until size) yield {
    Oeuvre(0, Image(0, rs,""), rs, "", Dimension(10, 10), 2020)
  }
}

object Remover {


  def removeFromChild(toRmeove: MenuItem, parent: MenuItem) = {
    parent.items = parent.items.filter(_.id != toRmeove.id)
  }

  def removeFromChild(toRmeove: Oeuvre, parent: MenuItem) = {
    parent.oeuvres = parent.oeuvres.filter(_.id != toRmeove.id)
  }

  @tailrec
  final def removeRec(toRmeove: MenuItem, newxr: List[MenuItem]): Unit = {
    if (newxr.isEmpty) {
      return ()
    } else {
      removeRec(toRmeove, if (newxr.nonEmpty) {
        val rem = removeFromChild(toRmeove, _: MenuItem)
        newxr.foreach(rem)
        newxr.flatMap(_.items)
      } else {
        Nil
      })
    }
  }

  @tailrec
  final def removeRec(toRmeove: Oeuvre, newxr: List[MenuItem]): Unit = {
    if (newxr.isEmpty) {
      return ()
    } else {
      removeRec(toRmeove, if (newxr.nonEmpty) {
        val rem = removeFromChild(toRmeove, _ : MenuItem)
        newxr.foreach(rem)
        newxr.flatMap(_.items)
      } else {
        Nil
      })
    }
  }
}

@JSExportTopLevel("SiteModel")
@JSExportAll
case class SiteModel(title: SiteTitle = SiteTitle(0, "Julia le Corre artiste")) {


  def remove(siteElement: SiteElement): Unit = {
    siteElement match {
      case a : Image => remove(a)
      case a : MenuItem => remove(a)
      case a : Oeuvre => remove(a)
    }
  }
  def remove(menuItem: MenuItem): Unit = Remover.removeRec(menuItem, items)

  def remove(o: Oeuvre): Unit = Remover.removeRec(o, items)

  def remove(i: Image): Unit = {}

  def allImages: List[Image] = items.flatMap(_.flatten[Image])

  def allOeuvres: List[Oeuvre] = items.flatMap(_.flatten[Oeuvre])


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