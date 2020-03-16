package bon.jo

import bon.jo.app.HtmlApp
import bon.jo.game.html.Template
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.Div

class TestSocketAppApp(app: Div, template: Template) extends HtmlApp[TestSocketTemplate](app: Div, template: Template) {

  val service = new SiteService

  val site: SiteModelView = SiteModelView(service.siteModel)(service)
  typedTemplate.site = site
  typedTemplate.service = service
  app.appendChild(site.html())

}
