package bon.jo.view

import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

case class ImgView(id:String, src : String, alt : String, divCss : String) extends FinalComponent[Div]{
  override def xml(): Node = <div id={id} class={divCss}><img src={src} alt={alt}/></div>

  override def init(parent: HTMLElement): Unit = {

  }
}
