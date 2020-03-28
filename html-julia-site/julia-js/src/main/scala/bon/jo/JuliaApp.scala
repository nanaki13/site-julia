package bon.jo

import bon.jo.app.{HtmlApp, User}
import bon.jo.game.html.Template
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Node


class JuliaApp(app: Div, template: Template) extends HtmlApp[SiteTemplate](app: Div, template: Template) {


  implicit val u = user

  val service = new SiteService
  val site: SiteModelView = SiteModelView(service.siteModel)(service)
  service.siteView = site
  typedTemplate.site = site
  typedTemplate.service = service
  app.appendChild(site.html())


}



