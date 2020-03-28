package bon.jo

import bon.jo.html.DomShell
import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws.GlobalExport
import bon.jo.service.SiteService
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.{Event, FileReader, HTMLElement, UIEvent}

import scala.scalajs.js.JSON
import scala.xml.Node

class ReadImportFile(implicit val siteService: SiteService, val template: SiteTemplate) extends FinalComponent[Input] {


  def read(): Unit = {
    val file = me.files(0);
    val reader = new FileReader();
    reader.readAsText(file, "UTF-8");
    reader.onload = (evt: UIEvent) => {

      reader.onerror = (evt: Event) => {
        DomShell.log("erreur reading file : " + JSON.stringify(evt))
      }
      siteService.importSite(JSON.parse(evt.target.asInstanceOf[FileReader].result.toString).asInstanceOf[GlobalExport])
      template.site.modelChange()
    }
  }

  override def xml(): Node = <input id={id} type="file" value="Import"></input>

  override def id: String = "import"

  override def init(parent: HTMLElement): Unit = {
    me.addEventListener("change", (_: Event) => read())
  }
}
