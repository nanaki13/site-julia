package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.inputXml
import bon.jo.html.Types.{FinalComponent, ParentComponent}
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Input, Link}
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

abstract class MenuItemView(val menuItem: MenuItem)(implicit val siteService: SiteService) extends FinalComponent[Div] with AdminControl[MenuItem] {

  override def service: DistantService[MenuItem,_] = siteService.menuService

  def cssClass : String
  val nomForm: Ref[Input] = Ref[Input](id + "nom")

  override def value: MenuItem = menuItem.copy(text = nomForm.value)

  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(menuItem, v)
    choose.removeFromView()
  })


  override def chooseMenuView: ValueView[MenuItem] with ParentComponent[Div] = choose

  def modifyView: Node = {
    <form>
      <form class="form">
        {inputXml(id + "nom", "nom", menuItem.text)}
      </form>
    </form>

  }


  override def xml(): Node =
    <div id={id}>
      <a class={cssClass} id={"btn-mi-" + id}>
        {menuItem.text}
      </a>{adminXmlOption match {
      case Some(value) => value
      case None =>
    }}
    </div>


  val link: Ref[Link] = Ref[Link]("btn-mi-" + id)

  override def init(p: HTMLElement): Unit = {

    initAdminEvent()

  }
}
