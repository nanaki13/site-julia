package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.{OnClick, XmlHtmlView}
import bon.jo.service.SiteService
import org.scalajs.dom.html.Div

import scala.xml.Node

case class SubMenuItemView(override val menuItem: MenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView)  extends MenuItemView(menuItem: MenuItem) {
  override def id: String = "si-"+menuItem.id
}