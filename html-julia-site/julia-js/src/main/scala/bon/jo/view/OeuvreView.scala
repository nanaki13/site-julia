package bon.jo.view

import bon.jo.{Logger, SiteModel}
import bon.jo.SiteModel.{MenuItem, Oeuvre}
import bon.jo.app.Response
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.inputXml
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{InDom, Types, ValueView}
import bon.jo.service.Raws.OeuvreRawExport
import bon.jo.service.{Raws, SiteService}
import org.scalajs.dom.html.{Div, Image, Input}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.xml.Node

case class OeuvreView(oeuvre: Oeuvre)(implicit val siteService: SiteService) extends
  FinalComponent[Div]
  with AdminControl[Oeuvre]
  with intOnce
  with WithImage[Div, Oeuvre] {

  override val service: DistantService[Oeuvre, OeuvreRawExport] = siteService.oeuvreService

  private val nomForm = Ref[Input](id + "nom")
  private val dateForm = Ref[Input](id + "date")
  private val descrpitionForm = Ref[Input](id + "description")
  private val xForm = Ref[Input](id + "x")
  private val yForm = Ref[Input](id + "y")

  implicit val executionContext: ExecutionContext = siteService.executionContext

  override def imageFor(e: Raws.ImageRawExport): Unit = {
    oeuvre.image = siteService.siteModel.allImages.find(_.id == e.id).get
    service.update(value).foreach(e => {
      updateSrc(oeuvre.image)
    })
  }

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
    }}<div class="oeuvre-text">
      <div class="text oeuvre-title">
        {oeuvre.name}
      </div>
      <div class="text">
        {oeuvre.date}
      </div>
      <div class="text">
        {oeuvre.dimension.x}
        cm x
        {oeuvre.dimension.y}
        cm
      </div>
      <div class="text">
        {oeuvre.description}
      </div>
    </div>
    <div class="img-cont">
      <div class="fore-ground"></div>
      <img id={"img-" + oeuvre.image.id} class="oeuvre-img"></img>
    </div>
  </div>

  override def id: String = "o-" + oeuvre.id





  override def factory: Ref[Image] = Ref[Image]("img-" + oeuvre.image.id)

  override val image: Option[SiteModel.Image] = Some(oeuvre.image)

  override def init(parent: HTMLElement): Unit = {
    initImg(parent)
    initAdminEvent()
  }



}



