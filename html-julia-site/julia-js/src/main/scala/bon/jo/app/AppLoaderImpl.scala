package bon.jo.app

import bon.jo._
import bon.jo.game.html.Template
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div

import scala.util.{Failure, Success}

object AppLoaderImpl extends App with AppLoader {


  val apps = List("app-game", "app-julia", "app-test", "app-login")

  val conf: Map[String, HtmlAppFactory[_]] = Map(


    "app-julia" -> new HtmlAppFactory[SiteTemplate]((app: Div, template: Template) => new JuliaApp(app, template), new SiteTemplate(_)),

    "app-login" -> new HtmlAppFactory[LoginTemplate]((app: Div, template: Template) => new LoginTemplateApp(app, template), new LoginTemplate(_))
  )

  import scala.concurrent.ExecutionContext.Implicits._




  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    Logger.log("loading apps")
    loadWithAuth(apps).map(e => {
      Logger.log(e.map(_.getClass).toString() + " loaded")
    }).onComplete {
      case Failure(exception) => exception.printStackTrace()
      case Success(value) => println("OK : may have application future  after ")
    }

  })
}














