package bon.jo.view

import bon.jo.SiteModel.ThemeMenuItem
import bon.jo.app.service.DistantService
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

abstract class ThemeMenuItemView( menuItem: ThemeMenuItem)(implicit  siteService: SiteService)  extends MenuItemView[ThemeMenuItem]( menuItem)(siteService) with   AdminControl[ThemeMenuItem,Int]{
  override def service: DistantService[ThemeMenuItem,_,Int] = siteService.menuService
  override def value: ThemeMenuItem = menuItem.copy(text = nomForm.value)
  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem("theme-for"+id,(v) => {

    siteService.move(menuItem, v)
    choose.removeFromView()
  })

  override def chooseMenuView: ValueView[ThemeMenuItem] with ParentComponent[Div] = choose


  override def xml(): Node =
    commonXml(adminXmlOption)

  override def init(p: HTMLElement): Unit = {
    initAdminEvent()
  }


}
