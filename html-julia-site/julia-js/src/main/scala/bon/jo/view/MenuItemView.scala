package bon.jo.view

import bon.jo.SiteModel.BaseMenuItem
import bon.jo.html.DomShell.inputXml
import bon.jo.html.Types.FinalComponent
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Input, Link}

import scala.xml.Node

abstract class MenuItemView[MI <: BaseMenuItem](val menuItem: MI)(implicit val siteService: SiteService) extends FinalComponent[Div] {


  def cssClass: String

  val nomForm: Ref[Input] = Ref[Input](id + "nom")


  def commonXml(beforeLink: Option[Node] = None) = <div id={id} class="configurable">
    {beforeLink match {
      case Some(value) => value
      case None =>
    }}<a class={cssClass} id={"btn-mi-" + id}>
      {menuItem.text}
    </a>
  </div>

  def modifyView: Node = {
    <form>
      <form class="form">
        {inputXml(id + "nom", "nom", menuItem.text)}
      </form>
    </form>

  }


  val link: Ref[Link] = Ref[Link]("btn-mi-" + id)


}
