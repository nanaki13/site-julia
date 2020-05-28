package bon.jo

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import bon.jo.SiteModel._

import scala.annotation.tailrec
import scala.collection.mutable
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
  abstract class SiteElement[ID](val id: ID) {
    implicit def toKeyValue: (ID, this.type) = id -> this


  }

  case class  TextId( uid: String,index : Int)
  @JSExportTopLevel("Text")
  @JSExportAll
  case class  Text(override val id: TextId,
   text:String) extends SiteElement[TextId](id)

  @JSExportTopLevel("Image")
  @JSExportAll
  case class Image(override val id: Int, link: String, base: String) extends SiteElement[Int](id)

  object Image {

    trait Ev[R <: SiteElement[B],B] extends (R => (B, R)) {
      override def apply(v1: R): (B, R) = v1.id -> v1
    }

    implicit object imageConv extends Ev[Image,Int]

  }

  @JSExportTopLevel("Oeuvre")
  @JSExportAll
  case class Oeuvre(override val id: Int, var image: Option[Image] = None, name: String = "A remplir", description: String = "A remplir", dimension: Dimension = Dimension(0,0), date: Int = (new Date()).getYear, theme: Option[ThemeMenuItem] = None) extends SiteElement(id) {
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

   class BaseMenuItem(override val id: Int, val text: String,val link: String) extends SiteElement(id)
  @JSExportTopLevel("MenuItem")
  @JSExportAll
  case class ThemeMenuItem(override val id: Int,override val  text: String,override val  link: String, var image: Option[Image], var parent: Option[ThemeMenuItem]
                           , var items: List[ThemeMenuItem] = List[ThemeMenuItem](),
                           var oeuvres: List[Oeuvre] = List[Oeuvre]()) extends BaseMenuItem(id,text,link) {
    def this(id: Int) = this(id, "", "", None, None)

    def this(text: String, link: String, parent: Option[ThemeMenuItem]) = this(0, text, link, None, parent)


    def randomOeuvre(size: Int): Unit = {
      oeuvres = SiteModel.randomOeuvre(size).toList
    }

    def flatten[R](implicit c: ClassTag[R]): List[R] = {
      (if (c == ClassTag(classOf[ThemeMenuItem])) {
        this :: items
      } else if (c == ClassTag(classOf[Image])) {
        val fromItem: List[Option[Image]] = this.items.flatMap(e => e.flatten[Image]).map(e => Some(e))
        val l: List[Option[Image]] = (this.image :: this.oeuvres.map(e => e.image)) ++ fromItem
        l.flatten
      } else {
        oeuvres ++ items.flatMap(e => e.flatten[Oeuvre])
      }).asInstanceOf[List[R]]
    }
  }

  def rs: String = Random.nextString(5)

  def randomOeuvre(size: Int): Seq[Oeuvre] = for (_ <- 0 until size) yield {
    Oeuvre(0, Some(Image(0, rs, "")), rs, "", Dimension(10, 10), 2020)
  }
}

object Remover {


  def removeFromChild(toRmeove: ThemeMenuItem, parent: ThemeMenuItem) = {
    parent.items = parent.items.filter(_.id != toRmeove.id)
  }

  def replaceFromChild(toReplace: Oeuvre, parent: ThemeMenuItem) = {
    val elAndId = parent.oeuvres.zipWithIndex.find(_._1.id == toReplace.id).get
    val nListArray = mutable.ArrayBuffer(parent.oeuvres: _ *)
    nListArray(elAndId._2) = toReplace
    parent.oeuvres = nListArray.toList
  }

  def removeFromChild(toRmeove: Oeuvre, parent: ThemeMenuItem) = {
    parent.oeuvres = parent.oeuvres.filter(_.id != toRmeove.id)
  }

  @tailrec
  final def removeRec(toRmeove: ThemeMenuItem, newxr: List[ThemeMenuItem]): Unit = {
    if (newxr.isEmpty) {
      ()
    } else {
      removeRec(toRmeove, if (newxr.nonEmpty) {
        val rem = removeFromChild(toRmeove, _: ThemeMenuItem)
        newxr.foreach(rem)
        newxr.flatMap(_.items)
      } else {
        Nil
      })
    }
  }

  @tailrec
  final def removeRec(toRmeove: Oeuvre, newxr: List[ThemeMenuItem]): Unit = {
    if (newxr.isEmpty) {
      return ()
    } else {
      removeRec(toRmeove, if (newxr.nonEmpty) {
        val rem = removeFromChild(toRmeove, _: ThemeMenuItem)
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
case class SiteModel() {


  def replace(mod: Oeuvre): Unit = {

  }


  def remove(siteElement: SiteElement[_]): Unit = {
    siteElement match {
      case a: Image => remove(a)
      case a: ThemeMenuItem => remove(a)
      case a: Oeuvre => remove(a)
    }
  }

  def remove(menuItem: ThemeMenuItem): Unit = Remover.removeRec(menuItem, items)

  def remove(o: Oeuvre): Unit = Remover.removeRec(o, items)

  def remove(i: Image): Unit = {}

  def allImages: List[Image] = items.flatMap(_.flatten[Image])

  def allOeuvres: List[Oeuvre] = items.flatMap(_.flatten[Oeuvre])


  var items: List[ThemeMenuItem] = List[ThemeMenuItem]()

  def add(menuItem: ThemeMenuItem): Unit = {
    items = items :+ menuItem
  }

  def allItem: Seq[ThemeMenuItem] = items.flatMap(_.flatten[ThemeMenuItem])


  def fake(): Unit = {
    val m1 = new ThemeMenuItem("Les a", "", None)
    m1.items = List(new ThemeMenuItem("Les a1", "", None), new ThemeMenuItem("Les a2", "", None), new ThemeMenuItem("Les a4", "", None))
    m1.items.foreach(_.randomOeuvre(Random.nextInt(5)))
    val mainMenu = List(
      AcceuilMenuItem,
      m1,
      ContactMenuItem
    )
    items = mainMenu
  }


}

object ContactMenuItem extends ThemeMenuItem(-1, "Contact", "/contact", None, None)

object AcceuilMenuItem extends ThemeMenuItem(-1, "Acceil", "/contact", None, None)