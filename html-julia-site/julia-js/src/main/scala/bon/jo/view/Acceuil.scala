package bon.jo.view

import bon.jo.SiteModel.Image
import bon.jo.app.service.DistantService
import bon.jo.html.Types.{FinalComponent, _Div}
import bon.jo.phy.MemoObs
import bon.jo.service.Raws.ImageRawExport
import bon.jo.service.SiteService
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.ExecutionContext
import scala.xml.{Elem, Node}

class Acceuil(imageService: DistantService[Image, ImageRawExport, Int])(implicit executionContext: ExecutionContext) extends _Div{



  val e = new MemoObs[List[ImgView]]
  imageService.getAll.map(e=> e.map(im => ImgView(im.id.toString,"img-acc",im)).toList).map(e.newValue)
  override def init(parent: HTMLElement): Unit = e.suscribe(_.foreach(_.addTo(me)))

  override def idXml: Elem = <div>

  </div>
}
