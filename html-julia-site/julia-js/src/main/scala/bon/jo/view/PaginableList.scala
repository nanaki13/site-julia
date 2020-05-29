package bon.jo.view

import bon.jo.Logger
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.InDom
import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, Node}

trait PaginableList[Finalp <: FinalComponent[_ <: HTMLElement]] extends SimpleList[Finalp] {

  implicit val ex: ExecutionContext
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

  def addedInView(el: Finalp): Unit

  def deletedInView(el: Finalp): Unit

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

  case class ScrollContext(previous: Div, next: Div, infoScroll: Div) {
    var init = false

    def asOption: Option[ScrollContext] = if (!init) {
      init = true
      Some(this)
    } else {
      None
    }

    def ifInit(e: ScrollContext => Unit): Unit = {
      asOption.foreach(e)
    }
  }


  lazy val scrollContext = {
    val next = $[Div](id + "-next")
    val prev = $[Div](id + "-prev")
    ScrollContext(prev, next, $[Div](id + "-info-scroll")
    )
  }

  override def xml(): Elem = <div id={id} class="list">
    {scrollElemn}{if (cssClass.isEmpty) <div id={"content" + id}></div> else <div id={"content" + id} class={cssClass}></div>}
  </div>

  @inline
  final def overScoll: Seq[Int] = 0 until option.maxByView

  @inline
  final def overOffsetIndex: Seq[Int] = overScoll.map(_ + option.offset)

  @inline
  final def overInPage: Seq[Int] = overOffsetIndex.filter(e => e >= 0 && e < currentView.size)

  @inline
  final def overCpntInPage: Seq[Finalp] = overInPage.map(currentView)

  def rebuild(finalp: Finalp): Finalp

  def next(e: MouseEvent): Unit = {

    if (maxPage != option.currentPage) {
      contentRef.ref.clear();
      overCpntInPage.foreach(deletedInView)
      for (i <- overInPage) {
        currentView(i) = rebuild(currentView(i))
      }
      option.currentPage += 1
      updateInfo()
      overCpntInPage.map(e => {
        e.addTo(contentRef.ref);
        e
      }).foreach {
        eee =>
          addedInView(eee)

      }

    }


  }

  import org.scalajs.dom.document

  def previous(e: MouseEvent): Unit = {
    if (1 != option.currentPage) {
      contentRef.ref.clear();

      overCpntInPage.foreach(deletedInView)
      for (i <- overInPage) {
        currentView(i) = rebuild(currentView(i))
      }
      option.currentPage -= 1
      updateInfo()
      overCpntInPage.map(e => {
        e.addTo(contentRef.ref);
        e
      }).
        foreach {
          eee =>
            addedInView(eee)

        }

    }

  }

  var addElement: Option[() => Future[Finalp]]


  override def clear(): Unit = {
    super.clear()
    scrollContext.infoScroll.style.display = "none"
  }

  override def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
    if(cps.nonEmpty){
      scrollContext.infoScroll.style.display = "block"
    }
    contentRef.ref.clear();
    currentView.foreach(deletedInView)

    currentView.clear()
    currentView++=cps
    option.currentPage = 1

    updateInfo()
    cps zip overScoll map (_._1) map (e => {
      e.addTo(contentRef.ref);
      e
    })
    addElement foreach (getEleemnt => {
      val buttonAdd = InDom[Div](<div class="btn" id={id + "add"} style="z-index=1001">AJOUTER</div>)
      buttonAdd.init(contentRef.ref)
      buttonAdd.me.clkOnce().suscribe(e =>
        getEleemnt().foreach(cpnt => {
          contentRef.ref.appendChild(cpnt.html())
          cpnt.init(contentRef.ref)
        })

      )
    })
    cps

  }

  def initEvent(ctx: ScrollContext): Unit = {
    ctx.ifInit(meAgain => {
      meAgain.previous.clk().suscribe(previous)
      meAgain.next.clk().suscribe(next)
    })

  }

  override def init(parentp: HTMLElement): Unit = {
    initEvent(scrollContext)
  }

}
