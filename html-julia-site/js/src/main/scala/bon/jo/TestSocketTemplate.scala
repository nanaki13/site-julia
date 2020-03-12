package bon.jo

import bon.jo.app.{Service, SiteModelView}
import bon.jo.game.html.Template

class TestSocketTemplate extends Template {

  val site : SiteModelView = SiteModelView(Service.siteModel)
  override def updateView(): Unit = {
    site.updateView()
  }
  override def body: String = site.xml().mkString


}
