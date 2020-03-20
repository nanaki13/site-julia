package bon.jo.view

import bon.jo.html.{DomShell, IdView, InDom, _View}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

trait Ref[Element <: HTMLElement] {
  val id: String

  lazy val ref: Element = DomShell.$(id)
}

object Ref {
  def apply[Element <: HTMLElement](idp: String): Ref[Element] = new Ref[Element] {
    override val id: String = idp
  }



}