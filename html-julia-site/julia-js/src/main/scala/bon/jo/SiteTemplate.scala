package bon.jo

import bon.jo.app.User
import bon.jo.game.html.Template
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{ButtonHtml, DomShell}
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.{Div, Link}
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.xml.{Elem, Node}

class SiteTemplate(override val user: User) extends Template {
  DomShell.log(s"chargement template avec $user")

  def info(playLoad: js.Any) = {

    $[Div]("ex").addChild(<div>
      {playLoad}
    </div>)
  }

  var service: SiteService = _


  private var _site: SiteModelView = _

  def site_=(s: SiteModelView): Unit = {

    _site = s
  }

  def site: SiteModelView = _site

  override def updateView(): Unit = {
    _site.updateView()


  }

  def admin: Boolean = user.role.admin

  def adminGlobalXml: Elem = <div id="admin-global">
    <div id="user"></div>
    <div id="ex"></div>
    <div id="im"></div>
    <div id="sa"></div>
  </div>

  override def body: String = (<div id="root">
    {if (admin) adminGlobalXml}
  </div>).mkString

  override def init(p: HTMLElement): Unit = {
    implicit val s = service
    implicit val t: SiteTemplate = this
    if (admin) {
      val importModel = new ReadImportFile
      val button = ButtonHtml("btn-export", "export")
      val buttonSaveAll = ButtonHtml("save-all", "Sauvegarder tout")
      button.onClick(_ => {
        DomShell.log("click export")
        val s: String = JSON.stringify(service.`export`)
        val dowload = Dowload(s)
        dowload.addTo("ex")
      })
      button.addTo("ex")
      importModel.addTo("im")
      buttonSaveAll.onClick(_ => {
        service.saveAll()
      })
      buttonSaveAll.addTo("sa")
      $[Div]("user").addChild(<span>Salut
        {user.name}
      </span>)
    }

    _site.init(me)

  }

  case class Dowload(s: String) extends FinalComponent[Link] {
    def sBase64: String = new String(java.util.Base64.getEncoder.encode(s.getBytes))

    override def xml(): Node = <a download="site.json" href={s"data:application/json;charset=utf-8;base64,$sBase64"}>text file</a>

    override def id: String = s.hashCode.toString

    override def init(parent: HTMLElement): Unit = {

    }
  }

  def toDownloable(s: String) = ""
}

