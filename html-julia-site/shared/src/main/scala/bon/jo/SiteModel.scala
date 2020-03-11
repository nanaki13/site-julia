package bon.jo

import java.util.concurrent.atomic.AtomicInteger
import scala.scalajs.js
object SiteModel {

  trait Id{
     val id : Int
  }
  trait IdProvider extends (() => Int)
  class ProvidedId extends IdProvider{
    var _id = new AtomicInteger
    override def apply(): Int =  _id.incrementAndGet
  }

  abstract class SiteElement(implicit idp : IdProvider) extends Id{
    override val id: Int = idp()
  }


  case class MenuItem(text : String, link : String )(implicit idp : IdProvider) extends  SiteElement

  case class SiteTitle(text : String)(implicit idp : IdProvider)  extends  SiteElement

  case class Image(link : String)(implicit idp : IdProvider)  extends  SiteElement

  case class Oeuvre(image: Image,name : String, dimension : Dimension, date : Int)(implicit idp : IdProvider)  extends  SiteElement

  case class Theme()(implicit idp : IdProvider)  extends  SiteElement

  case class Dimension(x : Float, y  :Float)(implicit idp : IdProvider)  extends  SiteElement
}
