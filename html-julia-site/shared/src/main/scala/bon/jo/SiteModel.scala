package bon.jo

import java.util.concurrent.atomic.AtomicInteger

import bon.jo.SiteModel.{IdProvider, MenuItem, SiteElement, SiteTitle}

import scala.annotation.meta.field
import scala.collection.mutable
import scala.scalajs.js
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
  abstract class SiteElement(id:Int) {
//    override val id: Int = idp()
  }



  @JSExportTopLevel("SiteTitle")
  @JSExportAll
  case class SiteTitle( id:Int,text: String)  extends SiteElement(id)

  @JSExportTopLevel("Image")
  @JSExportAll
  case class Image(id:Int, link: String) extends SiteElement(id)

  @JSExportTopLevel("Oeuvre")
  @JSExportAll
  case class Oeuvre(id:Int, image: Image, name: String, val dimension: Dimension, date: Int,theme : Option[Theme] = None) extends SiteElement(id)

  @JSExportTopLevel("Theme")
  @JSExportAll
  case class Theme(id:Int, name: String) extends SiteElement(id)

  @JSExportTopLevel("Dimension")
  @JSExportAll
  case class Dimension( x: Float,  y: Float)
  @JSExportTopLevel("MenuItem")
  @JSExportAll
  case class MenuItem(  text: String,  link: String, image: Option[Image] ){
    def this(text: String,  link: String) = this(text,link,None)
    var items: List[MenuItem] = List[MenuItem]()
    var oeuvres : List[Oeuvre] = List[Oeuvre]()
    def randomOeuvre(size : Int): Unit = {
      oeuvres = SiteModel.randomOeuvre(size).toList
    }
  }
  class ContactMenuItem extends MenuItem("Contact","/contact",None)
  def rs: String =  Random.nextString(5)
  def randomOeuvre(size : Int):Seq[Oeuvre] = for(_ <- 0 until size) yield{
    Oeuvre(0,Image(0,rs),rs,Dimension(10,10),2020)
  }
}

@JSExportTopLevel("SiteModel")
@JSExportAll
case class SiteModel(title : SiteTitle = SiteTitle(0,"Julia le Corre artiste")){

  var items: List[MenuItem] = List[MenuItem]()
  def add(menuItem: MenuItem): Unit = {
    items = items :+ menuItem
  }

  def fake(): Unit = {
    val m1 =  new MenuItem("Les a","")
    m1.items = List(new MenuItem("Les a1",""),new MenuItem("Les a2",""),new MenuItem("Les a4",""))
    m1.items.foreach(_.randomOeuvre(Random.nextInt(5)))
    val mainMenu = List(
      new MenuItem("Acceuil",""),
      m1,
      new MenuItem("Contact","")
    )
    items =mainMenu
  }


}

