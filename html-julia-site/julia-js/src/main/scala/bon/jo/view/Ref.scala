package bon.jo.view

import bon.jo.html.{DomShell, IdView, InDom, _View}
import com.sun.tools.javac.code.TypeTag
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.reflect.ClassTag

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