package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

class ChoooseMenuItem(valueConsumer: ValueConsumer[MenuItem])
                     (implicit val siteService: SiteService)
  extends ParentComponent[Div] with ValueView[MenuItem] {


  private var _value: MenuItem = _

  override def value(): MenuItem = _value

  override def init(parent: HTMLElement): Unit = {
    listens.added.foreach(e => {
      e.me.addEventListener("click", (ee: Event) => {
        _value = e.menuItem
        valueConsumer.consume(_value)
      })
    })
  }

  private var listens = SimpleTree[IdMenuItemVew]("c-li", siteService.siteModel.items.map(i => IdMenuItemVew(id + "-" + i.id, i)),
    e => {
      e.menuItem.items.map(i => IdMenuItemVew(id + "-" + i.id, i))
    }
  )

  override def xml(): Node = <div id={id}>
    {listens.xml()}
  </div>


  override def id: String = "choose"


}
