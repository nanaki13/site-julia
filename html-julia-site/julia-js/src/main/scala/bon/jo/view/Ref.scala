package bon.jo.view

import bon.jo.html.DomShell
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.HTMLElement

trait Ref[Element <: HTMLElement] {
  val id: String

  lazy val ref: Element = DomShell.$(id)

  def value: String = {

    this.ref match {
      case a: Input => a.value
      case _ => ""

    }
  }

}

object Ref {
  def apply[Element <: HTMLElement](idp: String): Ref[Element] = new Ref[Element] {
    override val id: String = idp


  }
}