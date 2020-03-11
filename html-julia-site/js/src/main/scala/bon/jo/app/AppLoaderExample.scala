package bon.jo.app

import bon.jo.SiteModel.ProvidedId
import bon.jo.html.DomShell._
import bon.jo._
import bon.jo.game.html.Template
import bon.jo.html.DomShell
import bon.jo.html.util.Anim
import bon.jo.test.Test
import org.scalajs.dom.document
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Element

object AppLoaderExample extends App {



  val idp = new ProvidedId
  DomShell.deb()
 println(idp.apply())
  val apps = List("app-game", "app-test-socket", "app-test")

  val conf: Map[String, HtmlAppFactory[_]] = Map(


    "app-test-socket" -> new HtmlAppFactory[TestSocketTemplate]((app: Div, template: Template) => new TestSocketAppApp(app, template), () => new TestSocketTemplate),
    "app-test" -> new HtmlAppFactory[Test]((app: Div, template: Template) => new Test(app, template), () => new Test)

  )

  /**
   * get the conf for an app and inject html from body template in element.
   * When it's done, call afterInDom on the template
   *
   * @param app
   * @param element
   * @return
   */
  def loadApp(app: String, element: Element): HtmlApp[Template] = {
    val confo: HtmlAppFactory[Template] = conf(app).asInstanceOf[HtmlAppFactory[Template]]
    val htmlFact = confo.htmlAppFactory
    val templateFact = confo.templateFactory
    val template = templateFact()
    element.innerHTML = template.body
    val appDiv: Div = $("root")
    val ret = htmlFact(appDiv, template)
    template.updateView()
    ret
  }

  /**
   * find the apps in html and load it
   *
   * @param apps
   */
  def loads(apps: List[String]): Unit = {
    for (app <- apps) {
      val appInit = document.getElementsByTagName(app)
      if (appInit != null && appInit.nonEmpty) {
        loadApp(app, appInit(0))
      }
    }
    Anim.start()
  }

  loads(apps)
  val appInit = document.getElementsByTagName("app-game")(0)





}
