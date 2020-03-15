package bon.jo.view

import bon.jo.html.{DomShell, _View}
import org.scalajs.dom.raw.HTMLElement

trait Ref[Element <: HTMLElement] {
  val id: String

  lazy val ref: Element = DomShell.$(id)
}

object Ref {
  def apply[Element <: HTMLElement](idp: String): Ref[Element] = new Ref[Element] {
    override val id: String = idp
  }

  trait RefComp[Element <: HTMLElement, ViewP <: _View[Element]] extends Ref[Element]{
    val view :  ViewP
  }

  def apply[Element <: HTMLElement, ViewP <: _View[Element]](viewp :ViewP):RefComp[Element,ViewP]  = new RefComp[Element,ViewP] {
    override val id: String = viewp.id
    override val view: ViewP =viewp
  }

}