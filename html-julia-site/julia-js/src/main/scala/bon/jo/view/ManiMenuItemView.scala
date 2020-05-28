package bon.jo.view

import bon.jo.SiteModel.ThemeMenuItem
import bon.jo.service.SiteService

case class ManiMenuItemView(override val menuItem: ThemeMenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView) extends ThemeMenuItemView(menuItem: ThemeMenuItem) {
  override def id: String = "mi-"+menuItem.id
  override def cssClass: String = "menu-item"



}
