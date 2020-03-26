package bon.jo.view

import bon.jo.SiteModel.Oeuvre
import bon.jo.html.DomShell.inputXml
import bon.jo.html.Types.FinalComponent
import bon.jo.service.{DistantService, SiteService}
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.xml.Node

case class OeuvreView(oeuvre: Oeuvre)(implicit val siteService: SiteService) extends FinalComponent[Div] with AdminControl[Oeuvre] {

  import siteService.imp._

  override val service: DistantService[Oeuvre] = siteService.oeuvreService

  private val nomForm = Ref[Input](id + "nom")
  private val dateForm = Ref[Input](id + "date")
  private val descrpitionForm = Ref[Input](id + "description")
  private val xForm = Ref[Input](id + "x")
  private val yForm = Ref[Input](id + "y")

  def extract: Oeuvre = oeuvre.copy(name = nomForm.ref.value, date = nomForm.ref.value.toInt, dimension = oeuvre.dimension.copy(xForm.ref.value.toFloat, yForm.ref.value.toFloat), description = descrpitionForm.ref.value.toString)

  override def value: Oeuvre = {
    extract
  }

  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(oeuvre, v)
    choose.removeFromView()
  })


  def modifyView: Node = {
    <form>
      <form class="form">
        {inputXml(id + "nom", "nom", oeuvre.name)}{inputXml(id + "date", "date", oeuvre.date)}{inputXml(id + "description", "description", oeuvre.description)}{inputXml(id + "x", "Largeur", oeuvre.dimension.x)}{inputXml(id + "y", "Hauteur", oeuvre.dimension.y)}
      </form>
    </form>

  }

  override def xml(): Node = <div id={id}>
    {adminXml}<div>
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
        {oeuvre.description}
      </div>
      <div>
        image:
        {oeuvre.image}<span class="btn btn-primary" id={"save-i-" + oeuvre.image.id}>save</span>
        <span class="btn btn-primary" id={"delete-i-" + oeuvre.image.id}>delete</span>
        <img src={oeuvre.image.base + oeuvre.image.link}></img>
      </div>
      <span class="btn btn-primary" id={"move-" + id}>Move</span> <span id={"choice-" + id}></span>
    </div>
  </div>

  override def id: String = "o-" + oeuvre.id

  override def updateView(): Unit = {}


  private val saveImageDiv = Ref[Div]("save-i-" + oeuvre.image.id)

  private val deleteImageDiv = Ref[Div]("delete-i-" + oeuvre.image.id)


  override def initAdminEvent(): Unit = {
    super.initAdminEvent()
    saveImageDiv.ref.addEventListener("click", (e: Event) => {
      siteService.imageService.save(oeuvre.image) foreach (_ => {
        saveImageDiv.ref.style.display = "none"
      })
    })
    deleteImageDiv.ref.addEventListener("click", (e: Event) => {
      siteService.imageService.delete(oeuvre.image.id) foreach (_ => {
        saveImageDiv.ref.style.display = "none"
      })
    })

  }

  override def init(parent: HTMLElement): Unit = {
    parent.appendChild(html())
    initAdminEvent()


  }
}