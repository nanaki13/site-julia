package bon.jo.view

import bon.jo.SiteModel.ThemeMenuItem
import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.{Elem, Node}

case class IdMenuItemVew(  id: String,  menuItem : ThemeMenuItem)

  extends FinalComponent[Div] {
  override def init(parent: HTMLElement): Unit = {}

  override def xml(): Elem = <div id={id}>
    <a class="btn" id={"btn-mi-"+id}>
      {menuItem.text}
    </a>
  </div>
}
