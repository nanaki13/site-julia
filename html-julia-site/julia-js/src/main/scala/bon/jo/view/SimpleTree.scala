package bon.jo.view

import bon.jo.html.Types.{FinalComponent, ParentComponent}
import org.scalajs.dom.html.Div

import scala.xml.{Elem, Group, Node}

case class SimpleTree[Finalp <: FinalComponent[_]](override val id: String, cps: List[Finalp], children: Finalp => List[Finalp]) extends ParentComponent[Div] {

  var added: List[Finalp] = Nil

  def register(finalp: Finalp) = {
    added = added :+ finalp
  }

  def xml(e: Finalp): Node = {
    register(e)
    <div>
      {e.xml}{val c = children(e)
    if (c.nonEmpty) {
      <div>
        {Group(c.map(e => xml(e)))}
      </div>
    } else {
      <div></div>
    }}
    </div>
  }

  override def xml(): Elem = <div id={id}>
    {if (cps.nonEmpty) {
      cps.map(xml)
    }}
  </div>
}
