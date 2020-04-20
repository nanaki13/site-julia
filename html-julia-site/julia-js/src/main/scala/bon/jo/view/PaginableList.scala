package bon.jo.view

import bon.jo.Logger
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

import scala.scalajs.js
import scala.xml.Node

trait PaginableList[Finalp <: FinalComponent[_ <: HTMLElement]] extends SimpleList[Finalp] {


  val option: OptionScroll = OptionScroll(
    maxByView = 10)

  def maxPage: Int = {

    val mewPage = (currentView.size / option.maxByView + (if (currentView.size % option.maxByView != 0) 1 else 0))

    mewPage
  }

  def updateInfo(): Unit = {
    $[Span](id + "-cp").textContent = option.currentPage.toString
    $[Span](id + "-totp").textContent = maxPage.toString
  }

  def addedInView(el: Finalp):Unit

  def deletedInView(el: Finalp):Unit

  def infoScroll: Node = <div>
    <div style="display:none">
      {currentView.size}
    </div>
    <div id={id + "-prev"} class="control-elm btn">
      &lt;
    </div>
    <div class="control-elm">
      <span id={id + "-cp"}>
        {option.currentPage}
      </span>
      /
      <span id={id + "-totp"}>
        {maxPage}
      </span>
    </div>
    <div id={id + "-next"} class="control-elm btn">
      &gt;
    </div>
  </div>

  def inScrolCtrl: Node = {
    <div id={id + "-info-scroll"}>
      {infoScroll}
    </div>
  }

  def scrollElemn: Node = <div id={id + "scroll-ctrl"} class="scroll-ctrl">
    {inScrolCtrl}
  </div>

  case class ScrollContext(previous: Div, next: Div, infoScroll: Div)


  lazy val scrollContext = {
    val next = $[Div](id + "-next")
    val prev = $[Div](id + "-prev")
    ScrollContext(prev, next, $[Div](id + "-info-scroll")
    )
  }

  override def xml(): Node = <div id={id} class="list">
    {scrollElemn}{if (cssClass.isEmpty) <div id={"content" + id}></div> else <div id={"content" + id} class={cssClass}></div>}
  </div>

  @inline
  final def overScoll: Seq[Int] = 0 until option.maxByView

  @inline
  final def overOffsetIndex: Seq[Int] = overScoll.map(_ + option.offset)

  @inline
  final def overInPage = overOffsetIndex.filter(e => e >= 0 && e < currentView.size)

  @inline
  final def overCpntInPage: Seq[Finalp] = overInPage.map(currentView)

  def next(e: MouseEvent): Unit = {

    if (maxPage != option.currentPage) {
      contentRef.ref.clear();

      overCpntInPage.foreach(deletedInView)
      option.currentPage += 1
      overCpntInPage.map(e => {
        e.addTo(contentRef.ref);
        e
      }).
        foreach {
          eee => addedInView(eee)
        }
      updateInfo()
    }


  }
  import org.scalajs.dom.{document, raw}
  def previous(e: MouseEvent): Unit = {
    if (1 != option.currentPage) {

      contentRef.ref.clear();
      overCpntInPage.foreach(e =>{
        deletedInView(e);
        Logger.log("after remove : "+e.id+ " : " + document.getElementById(e.id))
        Logger.log("after remove  admin-show" +e.id+ " : " + document.getElementById("admin-show" + e.id))
      })
      option.currentPage -= 1
      overCpntInPage.map(e => {

        e.addTo(contentRef.ref);
        e
      }).
        foreach {

          eee => Logger.log(eee); addedInView(eee)
        }
      updateInfo()
    }

  }

  override def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
    contentRef.ref.clear();
    currentView.foreach(deletedInView)
    currentView = cps
    option.currentPage = 1

    updateInfo()
    cps zip overScoll map (_._1) map (e => {
      e.addTo(contentRef.ref);
      e
    })


  }

  def initEvent(ctx: ScrollContext): Unit = {

    ctx.previous.clk().suscribe(previous)
    ctx.next.clk().suscribe(next)
  }

  override def init(parentp: HTMLElement): Unit = {
    initEvent(scrollContext)
  }

}
