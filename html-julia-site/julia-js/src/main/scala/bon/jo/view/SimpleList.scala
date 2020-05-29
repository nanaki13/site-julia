package bon.jo.view

import bon.jo.html.DomShell.ExtendedElement
import bon.jo.html.Types.{FinalComponent, ParentComponent}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.collection.mutable
import scala.xml.Elem

case class OptionScroll(maxByView: Int,  var currentPage: Int = 1){
  def offset: Int = (currentPage -1)* maxByView
}


object SimpleList{
  def apply[Finalp <: FinalComponent[_ <: HTMLElement]](idp : String,cssp : String): SimpleList[Finalp] = new SimpleList[Finalp]{
    override def cssClass: String = cssp

    override def id: String = idp
  }
}

trait SimpleList[Finalp <: FinalComponent[_ <: HTMLElement]]
  extends FinalComponent[Div] {



  def cssClass: String


  var currentView: mutable.ArrayBuffer[Finalp] = mutable.ArrayBuffer()

  def addAtEnd(cpnt : ParentComponent[_]): Unit ={
    contentRef.ref.addChild(cpnt.xml())
    cpnt.init( contentRef.ref)
  }

  override def xml(): Elem = <div id={id} class="list">
    {if (cssClass.isEmpty) <div id={"content" + id}></div> else <div id={"content" + id} class={cssClass}></div>}
  </div>


  val contentRef: Ref[Div] = Ref[Div]("content" + id)
  def clear(): Unit =  contentRef.ref.clear();

  def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
    contentRef.ref.clear();
    addAll(cps)
  }
  def addAll(cps: List[Finalp]): List[Finalp] = {
    cps.foreach(e => {
      e.addTo(contentRef.ref);
      e
    })
    cps
  }

  override def init(parentp: HTMLElement): Unit = {
  }
}
