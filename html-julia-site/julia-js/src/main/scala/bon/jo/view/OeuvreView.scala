package bon.jo.view

import bon.jo.Logger
import bon.jo.SiteModel.{MenuItem, Oeuvre}
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.inputXml
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{Types, ValueView}
import bon.jo.service.Raws.OeuvreRawExport
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Image, Input}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.concurrent.Future
import scala.scalajs.js.Promise
import scala.xml.Node

case class OeuvreView(oeuvre: Oeuvre)(implicit val siteService: SiteService) extends FinalComponent[Div] with AdminControl[Oeuvre] {

  override val service: DistantService[Oeuvre,OeuvreRawExport] = siteService.oeuvreService

  private val nomForm = Ref[Input](id + "nom")
  private val dateForm = Ref[Input](id + "date")
  private val descrpitionForm = Ref[Input](id + "description")
  private val xForm = Ref[Input](id + "x")
  private val yForm = Ref[Input](id + "y")

  def extract: Oeuvre = oeuvre.copy(name = nomForm.ref.value, date = dateForm.ref.value.toInt, dimension = oeuvre.dimension.copy(xForm.ref.value.toFloat, yForm.ref.value.toFloat), description = descrpitionForm.ref.value.toString)

  override def value: Oeuvre = {
    extract
  }


  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem((v) => {

    siteService.move(oeuvre, v)
    choose.removeFromView()
  })
  override def chooseMenuView: ValueView[MenuItem] with Types.ParentComponent[Div] = choose

  def modifyView: Node = {
    <form>
      <form class="form">
        {inputXml(id + "nom", "nom", oeuvre.name)}{inputXml(id + "date", "date", oeuvre.date)}{inputXml(id + "description", "description", oeuvre.description)}{inputXml(id + "x", "Largeur", oeuvre.dimension.x)}{inputXml(id + "y", "Hauteur", oeuvre.dimension.y)}
      </form>
    </form>

  }

  override def xml(): Node = <div class="oeuvre" id={id}>
    {adminXmlOption match {
      case Some(value) => value
      case None =>
    }}<div>
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
      <div><!--span class="btn btn-primary" id={"save-i-" + oeuvre.image.id}>save</span>
        <span class="btn btn-primary" id={"delete-i-" + oeuvre.image.id}>delete</span-->
        <img id={"img-"+ oeuvre.image.id} class="oeuvre-img" ></img>
      </div>
    </div>
  </div>

  override def id: String = "o-" + oeuvre.id

  override def updateView(): Unit = {}

  private val imgRef = Ref[Image]("img-"+ oeuvre.image.id)
  private val saveImageDiv = Ref[Div]("save-i-" + oeuvre.image.id)

  private val deleteImageDiv = Ref[Div]("delete-i-" + oeuvre.image.id)


  override def initAdminEvent(): Unit = {
    super.initAdminEvent()
//    saveImageDiv.ref.addEventListener("click", (e: Event) => {
//      siteService.imageService.save(oeuvre.image) foreach (_ => {
//        saveImageDiv.ref.style.display = "none"
//      })
//    })
//    deleteImageDiv.ref.addEventListener("click", (e: Event) => {
//      siteService.imageService.delete(oeuvre.image.id) foreach (_ => {
//        saveImageDiv.ref.style.display = "none"
//      })
//    })

  }

  var whenImageLoad :Future[Oeuvre] = Future.failed(new Exception("not started"))
  override def init(parent: HTMLElement): Unit = {
//    parent.appendChild(html())
    initAdminEvent()
    whenImageLoad = new Promise[Oeuvre]((accepet,reject)=>{
      imgRef.ref.addEventListener("load", (e : Event) => {
        Logger.log("Image loaded");
        imgRef.ref.classList.remove("loader")
        accepet(oeuvre)
      });
    }).toFuture

    imgRef.ref.classList.add("loader")
    imgRef.ref.src = oeuvre.image.base + oeuvre.image.link
  }


}