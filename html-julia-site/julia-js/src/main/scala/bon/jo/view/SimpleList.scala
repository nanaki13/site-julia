package bon.jo.view

import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement
import bon.jo.html.DomShell.ExtendedElement
import bon.jo.html.DomShell.ExtendedHTMLCollection
import scala.xml.Node

case class SimpleList[Finalp <: FinalComponent[_ <: HTMLElement]](override val id: String) extends FinalComponent[Div] {
  def loaded(): Unit = {
    loaderRef.ref.style.display = "none"
    contentRef.ref.style.display = "block"
  }
  def loading(): Unit = {
    loaderRef.ref.style.display = "block"
    contentRef.ref.style.display = "none"
  }

  override def xml(): Node = <div id={id}>
    <div id={"loader"+id} class="loader container"></div>
    <div id={"content"+id} class="column-display"></div>
  </div>

  val loaderRef = Ref[Div]("loader"+id)
  val contentRef = Ref[Div]("content"+id)
  def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
    contentRef.ref.clear();

    //  cps.foreach(e => me.appendChild(e.html().asInstanceOf[HTMLElement]));
    cps.foreach(e => {
      e.addTo(contentRef.ref); e
    })
    cps
    // init(parent)
  }

  override def init(parentp: HTMLElement): Unit = {
    parentp.appendChild(html());

  }
}
