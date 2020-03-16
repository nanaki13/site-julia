package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.{DomShell, OnClick}
import bon.jo.html.OnClick.BaseClick
import bon.jo.html.Types.FinalComponent
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Link}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.Node

abstract class MenuItemView(val menuItem: MenuItem)(implicit  val siteService: SiteService,siteModelView: SiteModelView) extends FinalComponent[Div]{

  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(menuItem,v)
    choose.removeFromView()
  })

  override def xml(): Node = <div id={id}>
    <a class="btn" id={"btn-mi-"+id}>
      {menuItem.text}
    </a> <span class="btn" id={"move-" + id}>Move</span><span id={"choice-" + id}></span>
  </div>

  private val moveDiv = Ref[Div]("move-" + id)
  private val choiceDiv = Ref[Div]("choice-" + id)


  val link: BaseClick[Link] =  OnClick[Link]("btn-mi-"+id)
  override def init(p: HTMLElement): Unit = {
    link.init(me)
    moveDiv.ref.addEventListener("click", (e: Event) => {
      choose.addTo(choiceDiv.ref)
    })

  }
}
