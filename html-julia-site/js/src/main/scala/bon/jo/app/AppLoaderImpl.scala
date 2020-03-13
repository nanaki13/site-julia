package bon.jo.app

import bon.jo.SiteModel.{Dimension, MenuItem, Oeuvre, ProvidedId}
import bon.jo.html.DomShell._
import bon.jo._
import bon.jo.game.html.Template
import bon.jo.html.{DomShell, InDom, OnClick, XmlHtmlView}
import bon.jo.html.util.Anim
import bon.jo.test.Test
import org.scalajs.dom.{Event, XMLHttpRequest, document}
import org.scalajs.dom.html.{Div, Link, Script}
import org.scalajs.dom.raw.Element

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSGlobal}
import scala.util.{Failure, Success, Try}
import scala.xml.{Group, Node}

object AppLoaderImpl extends App with AppLoader {


  val apps = List("app-game", "app-test-socket", "app-test")

  val conf: Map[String, HtmlAppFactory[_]] = Map(


    "app-test-socket" -> new HtmlAppFactory[TestSocketTemplate]((app: Div, template: Template) => new TestSocketAppApp(app, template), () => new TestSocketTemplate),
    "app-test" -> new HtmlAppFactory[Test]((app: Div, template: Template) => new Test(app, template), () => new Test)

  )


  org.scalajs.dom.window.addEventListener("load", (_: Event) => {
    DomShell.log("loaded")
    loads(apps)

  })
}

@JSGlobal("Service")
@js.native
object Service extends js.Object {
  val siteModel: SiteModel = js.native
}

case class OeuvreView(oeuvre: Oeuvre) extends XmlHtmlView[Div] with OnClick[Div] {

  override def xml(): Node = <div>
    <div id={id}>
      <div>
        {oeuvre.name}
      </div>
      <div>
        {oeuvre.date}
      </div>
      <div>
        {oeuvre.dimension}
      </div>
      <div>
        {oeuvre.image}
      </div>


    </div>
  </div>

  override def id: String = oeuvre.name.replaceAll("\\s+", "-")
}

case class ManiMenuItemView(menuItem: MenuItem) extends XmlHtmlView[Div] with OnClick[Div] {

  override def xml(): Node = <div>
    <a id={id}>
      {menuItem.text}
    </a>
  </div>

  override def id: String = menuItem.text.replaceAll("\\s+", "-")
}

case class SubMenuItemView(menuItem: MenuItem) extends XmlHtmlView[Div] with OnClick[Div] {

  override def xml(): Node = <div>
    <a id={id}>
      {menuItem.text}
    </a>
  </div>

  override def id: String = menuItem.text.replaceAll("\\s+", "-")
}

object DimensionConv {
  val reg = """\s*([^\s]+)\s*cm\s*x\s*([^\s]+)\s*cm\s*""".r

  def apply(s: String): Dimension = {
    s match {
      case reg(x, y) => Try( Dimension(x.trim.toFloat, y.trim.toFloat)) match {
        case Success(s) => s
        case Failure(f) => Dimension(x.trim.replace(",",".").toFloat, y.trim.replace(",",".").toFloat)
      }
      case _ => DomShell.log("cant parse : " + s); Dimension(0, 0)
    }
  }
}

object OeuvreConv {
  def apply(oeuvreRaw: OeuvreRaw): Oeuvre = new Oeuvre(null, oeuvreRaw.title, DimensionConv(oeuvreRaw.dimension), oeuvreRaw.date.toInt)
}

case class SiteModelView(model: SiteModel) extends XmlHtmlView[Div] with InDom {
  var mainContent: Div = _
  val itemsView: List[ManiMenuItemView] = model.items.map(ManiMenuItemView)

  override def xml(): Node = Group(<div id="side-menu">
    {itemsView.map(_.xml())}
  </div> <div id="current-view"></div>)

  override def id: String = "root"

  val AllOeuvre: oeuvres.type = oeuvres
  val AllTheme: themes.type = themes


  itemsView.foreach(i => {

    i.onClick((_: Event) => {
      var fresh: List[SubMenuItemView] = Nil
      mainContent.innerHTML = i.menuItem.items.map(SubMenuItemView).map(iHtml => {
        fresh = iHtml :: fresh
        iHtml.onClick((_: Event) => {
          mainContent.innerHTML = iHtml.menuItem.oeuvres.map(OeuvreView).map(_.xml().mkString).mkString
        })
        iHtml
      }).map(_.xml().mkString).mkString
      fresh.foreach(_.updateView())
    }
    )


  })

  override def updateView(): Unit = {
    mainContent = $[Div]("current-view")
    DomShell.deb()
    itemsView.foreach(_.updateView())
    val htmlO = AllOeuvre.map(OeuvreConv.apply).map(OeuvreView).map(_.html())
    htmlO.foreach(d => {
      mainContent.parentNode.appendChild(d)
    })
  }


}

@js.native
trait OeuvreRaw extends js.Object {
  val date: String
  val description: String
  val dimension: String
  val enable: String
  val id: String
  val image_key: String
  val tech_code: String
  val theme_key: String
  val title: String
}

@js.native
trait ThemeRaw extends js.Object {
  val id: String
  val name: String
}

@JSGlobal("oeuvres")
@js.native
object oeuvres extends js.Array[OeuvreRaw]

@JSGlobal("themes")
@js.native
object themes extends js.Array[ThemeRaw]