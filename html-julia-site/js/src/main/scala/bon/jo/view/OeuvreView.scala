package bon.jo.view

import bon.jo.SiteModel.Oeuvre
import bon.jo.html.{InDom, OnClick, XmlHtmlView}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

case class OeuvreView(oeuvre: Oeuvre) extends XmlHtmlView[Div] with InDom[Div] {

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
        image:  {oeuvre.image}
        <img src={"http://julia-le-corre.fr/rsc/"+oeuvre.image.link }></img>
      </div>
    </div>
  </div>

  override def id: String =  "o-" + oeuvre.id

  override def updateView(): Unit = {}

  override def init(parent: HTMLElement): Unit = {}
}
