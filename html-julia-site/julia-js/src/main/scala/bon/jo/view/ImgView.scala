package bon.jo.view

import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws.ImageRawExport
import org.scalajs.dom.html.{Div, Image}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.{Elem, Node}

case class ImgView(id: String, divCss: String,iRaw: ImageRawExport) extends FinalComponent[Div] {

  val src: String = iRaw.base + "/" + iRaw.link
  val alt: String = "img-" + iRaw.id

  override def xml(): Elem = <div id={id} class={divCss}>
    <img id={"mg" + id} alt={alt}/>
  </div>

  var img = Ref[Image]("mg" + id)

  var loaded = false;

  override def init(parent: HTMLElement): Unit = {
    if (!loaded) {
      img.ref.addEventListener("load", (e: Event) => {
        img.ref.classList.remove("loader")
        loaded = true;
      });
      img.ref.classList.add("loader")

    } else {
      img = Ref[Image]("mg" + id)
    }
    img.ref.src = src
  }
}
object ImgView{
  def apply(other : ImgView): ImgView = other.copy()
}