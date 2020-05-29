package bon.jo.view

import bon.jo.SiteModel
import bon.jo.SiteModel.ThemeMenuItem
import bon.jo.service.{Raws, SiteService}
import org.scalajs.dom.html.{Div, Image}
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.ExecutionContext
import scala.xml.{Elem, Group, Node}

case class SubMenuItemView(override val menuItem: ThemeMenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView)
  extends ThemeMenuItemView(menuItem: ThemeMenuItem) with intOnce
    with WithImage[Div, ThemeMenuItem,Int] {
  override def id: String = "si-" + menuItem.id

  override def cssClass: String = "sub-menu-item"

  implicit val executionContext: ExecutionContext = siteService.executionContext

  override def imageFor(e: Raws.ImageRawExport): Unit = {
    menuItem.image = siteService.siteModel.allImages.find(_.id == e.id)
    if (image.isEmpty) {
      menuItem.image = Some(siteService.createImage(e))
    }
    service.update(menuItem).foreach(ee => {

      updateSrc(menuItem.image.get)
    })

  }
  private def l = adminXmlOption.foldLeft(List[Node]())(_ :+ _)
  private def img = value.image.map(i => <img id={"img-m-" + id} class="oeuvre-img"></img>).getOrElse(<img id={"img-m-" + id} class="oeuvre-img" alt="Choisi moi une image"></img>)
  private def all = l :+ img
  override def xml(): Elem = {
    commonXml(Some(Group(all)))
  }


  override def init(p: HTMLElement): Unit = {
    super.init(p)
    initImg(p)


  }


  override def factory: Option[Ref[Image]] = image.map(_ => Ref[Image]("img-m-" + id))

  override def image: Option[SiteModel.Image] = value.image
}