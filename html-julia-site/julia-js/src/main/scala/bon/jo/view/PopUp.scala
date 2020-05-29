package bon.jo.view

import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{CSSStyleDeclaration, HTMLElement}
import org.w3c.dom.css.{CSSStyleDeclaration, CSSStyleRule}

import scala.xml.{Elem, Node}

object PopUp extends FinalComponent[Div] {
  override def xml(): Elem = <div id="popup">

  </div>

  override def init(parent: HTMLElement): Unit = {

  }

  override def id: String = {
    "popup"
  }
}
case class PopUp(str: String) {
  def buildCss( e : HTMLElement) {
    val css = e.style
    css.display = "block"
    css.position = "fixed"
    css.top = "10em"
    css.right = "10em"
    css.width = "9em"
    css.transition = "transition: 7s ease-in-out;"
    css.overflow = "hidden"
    css.backgroundColor="#FFFA51"
  }




  if (!PopUp.isInDom) {
    val html = PopUp.html()
   buildCss(html)
    org.scalajs.dom.document.body.appendChild(html)
  }
  PopUp.me.innerText = str
  PopUp.me.style.width = "9em"
  PopUp.me.style.height = "auto"
  scalajs.js.timers.setTimeout(7000)({PopUp.me.style.width = "0";PopUp.me.style.height = "0"})
}

