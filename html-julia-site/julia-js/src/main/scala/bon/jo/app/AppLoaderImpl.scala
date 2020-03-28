package bon.jo.app

import java.io.{OutputStream, OutputStreamWriter, PrintStream}

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


    "app-julia" -> new HtmlAppFactory[SiteTemplate]((app: Div, template: Template) => new JuliaApp(app, template),  new SiteTemplate(_)),

    "app-login" -> new HtmlAppFactory[LoginTemplate]((app: Div, template: Template) => new LoginTemplateApp(app, template), new LoginTemplate(_))
  )

  import scala.concurrent.ExecutionContext.Implicits._

  val req = GET.send("/api/menu").onComplete {
    case Failure(exception) => println(exception)
    case Success(value) =>println("BODY : "+value.body)
  }



  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    DomShell.log("loading apps")
       loadWithAuth(apps).map( e=> {
         println(e.map(_.getClass)+" loaded")
       }).onComplete {
         case Failure(exception) => exception.printStackTrace()
         case Success(value) =>println("OK : "+value)
       }

  })
}














