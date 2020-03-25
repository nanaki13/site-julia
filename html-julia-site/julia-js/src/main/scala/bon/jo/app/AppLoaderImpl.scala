package bon.jo.app

import bon.jo._
import bon.jo.app.RequestHttp.GET
import bon.jo.game.html.Template
import bon.jo.html.DomShell
import bon.jo.test.Test
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div

import scala.util.{Failure, Success}

object AppLoaderImpl extends App with AppLoader {


  val apps = List("app-game", "app-julia", "app-test", "app-login")

  val conf: Map[String, HtmlAppFactory[_]] = Map(


    "app-julia" -> new HtmlAppFactory[SiteTemplate]((app: Div, template: Template) => new JuliaApp(app, template), () => new SiteTemplate),
    "app-test" -> new HtmlAppFactory[Test]((app: Div, template: Template) => new Test(app, template), () => new Test),
    "app-login" -> new HtmlAppFactory[LoginTemplate]((app: Div, template: Template) => new LoginTemplateApp(app, template), () => new LoginTemplate)
  )

  import scala.concurrent.ExecutionContext.Implicits._

  val req = new NEWRequestHttp("/api/menu", GET)
  val oIt = req.sendBody(null)
  println(oIt)
  oIt.onComplete {
    case Failure(exception) => println(exception)
    case Success(value) =>println(value.body)
  }
  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    DomShell.log("loading apps")
    //loads(apps)

  })
}














