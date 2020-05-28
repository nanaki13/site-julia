package bon.jo.view

import bon.jo.{Logger, SiteModel}
import bon.jo.SiteModel.{Oeuvre, ThemeMenuItem}
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.{$, ExtendedElement, inputXml}
import bon.jo.html.Types.FinalComponent
import bon.jo.html.{Types, ValueView}
import bon.jo.service.Raws.OeuvreRawExport
import bon.jo.service.{Raws, SiteService}
import org.scalajs.dom.html.{Div, Image, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.ExecutionContext
import scala.xml.{Group, Node, NodeBuffer, NodeSeq}

case class OeuvreView(var oeuvre: Oeuvre)(implicit val siteService: SiteService) extends
  FinalComponent[Div]
  with AdminControl[Oeuvre,Int]
  with intOnce
  with WithImage[Div, Oeuvre,Int] {

  override val service: DistantService[Oeuvre, OeuvreRawExport,Int] = siteService.oeuvreService

  private val nomForm = Ref[Input](id + "nom")
  private val dateForm = Ref[Input](id + "date")
  private val descrpitionForm = Ref[Input](id + "description")
  private val xForm = Ref[Input](id + "x")
  private val yForm = Ref[Input](id + "y")

  implicit val executionContext: ExecutionContext = siteService.executionContext

  override def imageFor(e: Raws.ImageRawExport): Unit = {
    oeuvre.image = siteService.siteModel.allImages.find(_.id == e.id)

    service.update(value).foreach(e => {
      oeuvre.image.foreach(updateSrc)
      obs.newValue(oeuvre)
    })
  }



  override def value: Oeuvre = {
    Logger.log(oeuvre.image+" In value of oezuver")
    oeuvre.copy(name = nomForm.ref.value,
      date = dateForm.ref.value.toInt,
      dimension = oeuvre.dimension.copy(xForm.ref.value.toFloat,
        yForm.ref.value.toFloat),
      description = descrpitionForm.ref.value)
  }


  lazy val choose: ChoooseMenuItem = new ChoooseMenuItem("theme-for" + id, (v) => {

    siteService.move(oeuvre, v).foreach(e => {

      removeFromView()
    })

  })

  override def chooseMenuView: ValueView[ThemeMenuItem] with Types.ParentComponent[Div] = choose

  def modifyView: Node = {
    <form>
      <form class="form">
        {inputXml(id + "nom", "nom", oeuvre.name)}{inputXml(id + "date", "date", oeuvre.date)}{inputXml(id + "description", "description", oeuvre.description)}{inputXml(id + "x", "Largeur", oeuvre.dimension.x)}{inputXml(id + "y", "Hauteur", oeuvre.dimension.y)}
      </form>
    </form>

  }

  def inMe: Node = {

    <div id={id + "-disp"}>

      <div class="oeuvre-text">
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
        <img id={idImage} class="oeuvre-img"></img>
      </div>
    </div>

  }

  override def xml(): Node = {
    val l = adminXmlOption match {
      case Some(value) => List(inMe, value)
      case None => List(inMe)
    }
    val n = NodeSeq.fromSeq(l)
    <div class="oeuvre configurable" id={id}>
      {Group(n)}
    </div>
  }

  override def id: String = "o-" + oeuvre.id


  def idImage: String = s"img-$id-" + oeuvre.image.map(_.id).getOrElse("undef")


  override def factory: Option[Ref[Image]] = Some(Ref[Image](idImage))

  override def image: Option[SiteModel.Image] =
    oeuvre.image

  override def init(parent: HTMLElement): Unit = {
    initImg(parent)
    initAdminEvent()

    obs.suscribe(o => {


      Logger.log(o.name)
      oeuvre = o
      val getDisp = $[Div](id + "-disp")
      getDisp.clear
      getDisp.addChild(inMe)
      _imgRef = None
      initImg(parent)
    })
  }


}



