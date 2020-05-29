package bon.jo.view

import bon.jo.SiteModel.ThemeMenuItem
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.{Elem, Node}

case class ChoooseMenuItem(  id:String,valueConsumer: ValueConsumer[ThemeMenuItem])
                     (implicit val siteService: SiteService)
  extends ParentComponent[Div] with ValueView[ThemeMenuItem] {


  private var _value: ThemeMenuItem = _

  override def value(): ThemeMenuItem = _value

  override def init(parent: HTMLElement): Unit = {
    listens.added.foreach(e => {
      e.me.addEventListener("click", (ee: Event) => {
        _value = e.menuItem

        valueConsumer.consume(_value)
      })
    })
  }

  private val listens = SimpleTree[IdMenuItemVew]("c-li", siteService.siteModel.items.map(i => IdMenuItemVew(id + "-" + i.id, i)),
    e => {
      e.menuItem.items.map(i => IdMenuItemVew(id + "-" + i.id, i))
    }
  )

  override def xml(): Elem = <div id={id}>
    {listens.xml()}
  </div>





}
