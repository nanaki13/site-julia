package bon.jo.view

import bon.jo.html.DomShell.$
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{ButtonHtml, DomShell, OnClick}
import org.scalajs.dom.html.{Button, Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

case class SimpleInput(id: String, label: String, valueIni: Any = "") extends FinalComponent[Div] {
  private val inputXml = DomShell.inputXml(id, label, valueIni)
  val confirm: OnClick[Button] = ButtonHtml("ok-"+id, "ok")

  var inputHtml: Input = _

  override def xml(): Node = <div>
    {inputXml}<div id={"send-"+id}></div>
  </div>

  override def init(parent : HTMLElement): Unit = {

    confirm.addTo("send-"+id)
    inputHtml = $(id)
  }

  def value(): String = inputHtml.value
}
