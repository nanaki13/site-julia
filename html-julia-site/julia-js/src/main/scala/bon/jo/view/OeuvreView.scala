package bon.jo.view

import bon.jo.SiteModel.Oeuvre
import bon.jo.html.Types.FinalComponent
import bon.jo.service.SiteService
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.Node

case class OeuvreView(oeuvre: Oeuvre)(implicit  val siteService: SiteService)  extends FinalComponent[Div] {
  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(oeuvre,v)
    choose.removeFromView()
  })
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
      <span class="btn btn-primary" id={"move-" + id}>Move</span><span id={"choice-" + id}></span>
    </div>
  </div>

  override def id: String =  "o-" + oeuvre.id

  override def updateView(): Unit = {}
  private val moveDiv = Ref[Div]("move-" + id)
  private val choiceDiv = Ref[Div]("choice-" + id)

  override def init(parent: HTMLElement): Unit = {
    parent.appendChild(html())
    moveDiv.ref.addEventListener("click", (e: Event) => {

      choose.addTo(choiceDiv.ref)

    })
  }
}
