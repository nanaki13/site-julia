package bon.jo

import bon.jo.app.{HtmlApp, User}
import bon.jo.game.html.Template
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Node




class JuliaApp(app: Div, template: Template) extends HtmlApp[SiteTemplate](app: Div, template: Template) {










  def afterAuth(user: User): Node = {
    val service = new SiteService(user)
    val site: SiteModelView = SiteModelView(service.siteModel)(service)
    service.siteView = site
    typedTemplate.site = site
    typedTemplate.service = service
    app.appendChild(site.html())
  }




}



