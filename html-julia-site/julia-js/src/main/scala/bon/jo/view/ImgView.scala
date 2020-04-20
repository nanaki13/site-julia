package bon.jo.view

import bon.jo.SiteModel.Oeuvre
import bon.jo.html.DomShell.$
import bon.jo.html.Types.FinalComponent
import bon.jo.service.Raws.ImageRawExport
import org.scalajs.dom.html.{Div, Image}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.xml.Node

case class ImgView(id: String, src: String, alt: String, divCss: String,imageRawExport: ImageRawExport) extends FinalComponent[Div] {
  override def xml(): Node = <div id={id} class={divCss}>
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
