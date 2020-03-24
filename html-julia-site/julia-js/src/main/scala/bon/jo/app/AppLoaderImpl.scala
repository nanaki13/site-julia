package bon.jo.app

import java.util.Base64

import bon.jo._
import bon.jo.game.html.Template
import bon.jo.html.{ButtonHtml, DomShell}
import bon.jo.service.RequestHttp.GET
import bon.jo.test.Test
import bon.jo.view.Ref
import org.scalajs.dom.Event
import org.scalajs.dom.html.{Button, Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.xml.Group

object AppLoaderImpl extends App with AppLoader {


  val apps = List("app-game", "app-julia", "app-test", "app-login")

  val conf: Map[String, HtmlAppFactory[_]] = Map(


    "app-julia" -> new HtmlAppFactory[SiteTemplate]((app: Div, template: Template) => new JuliaApp(app, template), () => new SiteTemplate),
    "app-test" -> new HtmlAppFactory[Test]((app: Div, template: Template) => new Test(app, template), () => new Test),
    "app-login" -> new HtmlAppFactory[LoginTemplate]((app: Div, template: Template) => new LoginTemplateApp(app, template), () => new LoginTemplate)
  )


  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    DomShell.log("loading apps")
    loads(apps)

  })
}














