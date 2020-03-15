package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.{OnClick, XmlHtmlView}
import bon.jo.service.SiteService
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.Node

abstract class MenuItemView(val menuItem: MenuItem)(implicit  val siteService: SiteService,siteModelView: SiteModelView) extends XmlHtmlView[Div] with OnClick[Div] {

  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(menuItem,v)
    choose.removeFromView()
  })

  override def xml(): Node = <div>
    <a class="btn" id={id}>
      {menuItem.text}
    </a> <span class="btn" id={"move-" + id}>Move</span><span id={"choice-" + id}></span>
  </div>

  private val moveDiv = Ref[Div]("move-" + id)
  private val choiceDiv = Ref[Div]("choice-" + id)

  override def init(p: HTMLElement): Unit = {
    super.init(p)
    moveDiv.ref.addEventListener("click", (e: Event) => {
      choose.addTo(choiceDiv.ref)
    })

  }
}
