package bon.jo.app

import bon.jo.SiteModel.{MenuItem, ProvidedId}
import bon.jo.html.DomShell._
import bon.jo._
import bon.jo.game.html.Template
import bon.jo.html.{DomShell, InDom, OnClick, XmlHtmlView}
import bon.jo.html.util.Anim
import bon.jo.test.Test
import org.scalajs.dom.{Event, document}
import org.scalajs.dom.html.{Div, Link}
import org.scalajs.dom.raw.Element

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}
import scala.xml.{Group, Node}

object AppLoaderExample extends App {


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


  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    DomShell.log("loaded")
    loads(apps)
    val appInit = document.getElementsByTagName("app-game")(0)
  })
}

@JSGlobal("Service")
@js.native
object Service extends js.Object {
  val siteModel: SiteModel = js.native
}

case class ManiMenuItemView(menuItem: MenuItem) extends XmlHtmlView[Div] with OnClick[Div] {

  override def xml(): Node = <div><a id={id}>
    {menuItem.text}
  </a></div>

  override def id: String = menuItem.text.replaceAll("\\s+", "-")
}

case class SiteModelView(model: SiteModel) extends XmlHtmlView[Div] with InDom {
  var d: Div = _
  val itemsView: List[ManiMenuItemView] = model.items.map(ManiMenuItemView)

  override def xml(): Node = Group(<div id="side-menu">
    {itemsView.map(_.xml())}
  </div> <div id="current-view"></div>)

  override def id: String = "root"

  itemsView.foreach(i => {
    DomShell.log(i.id)
    i.onClick((_: Event) => {
      DomShell.deb()
      d.innerHTML = i.menuItem.items.map(ManiMenuItemView).map(_.xml().mkString).mkString
    }
    )
  })

  override def updateView(): Unit = {
    d = $[Div]("current-view")
    DomShell.deb()
    itemsView.foreach(_.updateView())
  }
}