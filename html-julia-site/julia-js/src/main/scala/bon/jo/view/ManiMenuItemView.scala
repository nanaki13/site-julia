package bon.jo.view

import bon.jo.Logger
import bon.jo.SiteModel.MenuItem
import bon.jo.service.{Raws, SiteService}

case class ManiMenuItemView(override val menuItem: MenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView) extends MenuItemView(menuItem: MenuItem) {
  override def id: String = "mi-"+menuItem.id
  override def cssClass: String = "menu-item"



}
