package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.service.SiteService

case class SubMenuItemView(override val menuItem: MenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView)  extends MenuItemView(menuItem: MenuItem) {
  override def id: String = "si-"+menuItem.id

  override def cssClass: String = "sub-menu-item"
}