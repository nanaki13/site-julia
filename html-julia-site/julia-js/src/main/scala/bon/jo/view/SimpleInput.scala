package bon.jo.view

import bon.jo.html.ButtonHtml.ButtonType
import bon.jo.html.DomShell.$
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{ButtonHtml, DomShell}
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.xml.Node

case class SimpleInput(id: String, label: String, valueIni: String = "",title : Option[String] = None) extends FinalComponent[Div] {
  private val inputXml = DomShell.inputXml(id, label, valueIni)
  val confirm: ButtonType = ButtonHtml("ok-"+id, "ok")

  var inputHtml: Input = _

  override def xml(): Node = <div  id={id} >
    {title match {
      case Some(s) =>s
      case _ =>
    }
    }
    {inputXml}<div id={"send-"+id}></div>
  </div>

  override def init(parent : HTMLElement): Unit = {

    confirm.addTo("send-"+id)
    inputHtml = $(id)
  }

  def value(): String = inputHtml.value
}
