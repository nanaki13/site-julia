package bon.jo.view

import bon.jo
import bon.jo.SiteModel
import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws.ImageRawExport
import bon.jo.service.RawsObject.ImageRawExport
import org.scalajs.dom.html.{Div, Image}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.{Elem, Node}

case class ImgView(id: String, divCss: String,iRaw: ImageRawExport = ImageRawExport.apply(new jo.SiteModel.Image(1,"","")),imgClass :String = "") extends FinalComponent[Div] {

  val src: String = iRaw.base + "/" + iRaw.link
  val alt: String = "img-" + iRaw.id

  override def xml(): Elem = <div id={id} class={divCss}>
    <img id={"img-" + id} alt={alt} class={imgClass}/>
  </div>

  var img = Ref[Image]("img-"+ id)

  var loaded = false;

  override def init(parent: HTMLElement): Unit = {
    if (!loaded) {
      img.ref.addEventListener("load", (e: Event) => {
        img.ref.classList.remove("loader")
        loaded = true;
      });
      img.ref.classList.add("loader")

    } else {
      img = Ref[Image]("img-" + id)
    }
    img.ref.src = src
  }
}
object ImgView{
  def apply(other : ImgView): ImgView = other.copy()
}