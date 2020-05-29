package bon.jo.view

import bon.jo.Logger
import bon.jo.html.OnClick.ButtonType
import bon.jo.html.DomShell.$
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{DomShell, OnClick}
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.xml.{Elem, Node}

case class SimpleInput(id: String, label: String, valueIni: String = "",title : Option[String] = None) extends FinalComponent[Div] {
  private val inputXml = DomShell.inputXml(id+"-input", label, valueIni)
  val confirm: ButtonType = OnClick("ok-"+id, "ok")

  var inputHtml: Input = _

  override def xml(): Elem = <div  id={id} >
    {title match {
      case Some(s) =>s
      case _ =>
    }
    }
    {inputXml}<div id={"send-"+id}></div>
  </div>

  override def init(parent : HTMLElement): Unit = {

    confirm.addTo("send-"+id)

    inputHtml = $(id+"-input")
  }

  def value(): String = {
    if(inputHtml == null){

      throw new Exception("pas d'input")
    }else{

      Logger.log(inputHtml)
      inputHtml.value
    }
  }
}
