package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

case class IdMenuItemVew(  id: String,  menuItem : MenuItem)

  extends FinalComponent[Div] {
  override def init(parent: HTMLElement): Unit = {}

  override def xml(): Node = <div id={id}>
    <a class="btn" id={"btn-mi-"+id}>
      {menuItem.text}
    </a>
  </div>
}
