package bon.jo

import bon.jo.game.html.Template
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{ButtonHtml, DomShell}
import bon.jo.service.Raws.GlobalExport
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.html.{Input, Link}
import org.scalajs.dom.raw.{Event, FileReader, HTMLElement, UIEvent}

import scala.scalajs.js.JSON
import scala.xml.Node

class TestSocketTemplate extends Template {
   var service: SiteService = _


  private var _site: SiteModelView = _

  def site_=(s: SiteModelView): Unit = {

    _site = s
  }

  def site: SiteModelView = _site

  override def updateView(): Unit = {
    _site.updateView()


  }

  val button = ButtonHtml("btn-export", "export")


  override def body: String = (<div id="root">
    <div id="ex"></div>
    <div id="im"></div>
  </div>).mkString

  override def init(p: HTMLElement): Unit = {
    implicit val s = service
    implicit val t : TestSocketTemplate = this
    val importModel = new ReadFile
    button.onClick(_ => {
      DomShell.log("click export")
      val s: String = JSON.stringify(service.`export`)
      val dowload = Dowload(s)
      dowload.addTo("ex")
    })
    button.addTo("ex")
    importModel.addTo("im")

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

class ReadFile(implicit val siteService: SiteService,val template: TestSocketTemplate) extends FinalComponent[Input] {



  def read(): Unit = {
    val file = me.files(0);
    val reader = new FileReader();
    reader.readAsText(file, "UTF-8");
    reader.onload = (evt: UIEvent) => {

      reader.onerror = (evt: Event) => {
        DomShell.log("erreur reading file : " + JSON.stringify(evt))
      }
      siteService.importSite( JSON.parse( evt.target.asInstanceOf[FileReader].result.toString).asInstanceOf[GlobalExport])
      template.site.modelChange()
    }
  }

  override def xml(): Node = <input id={id} type="file" value="Import"></input>

  override def id: String = "import"

  override def init(parent: HTMLElement): Unit = {
    me.addEventListener("change", (_: Event) => read())
  }
}