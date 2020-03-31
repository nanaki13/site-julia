package bon.jo

import bon.jo.app.{HtmlApp, User}
import bon.jo.game.html.Template
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.Div

import scala.concurrent.{ExecutionContext, Future}


class JuliaApp(app: Div, template: Template) extends HtmlApp[SiteTemplate](app: Div, template: Template) {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val u: User = user

  implicit val service: SiteService = new SiteService


  override def asynStartup(): Future[Unit] = service.getGlobalExport.map(siteData => {
    Logger.log(siteData)
    service.siteModel.items = service.toSiteModelElements(siteData).toList
    val site: SiteModelView = SiteModelView(service.siteModel)
    service.siteView = site
    typedTemplate.site = site
    typedTemplate.service = service
    app.appendChild(site.html())
  })

  override def haveAsynStartup: Boolean = true
}



