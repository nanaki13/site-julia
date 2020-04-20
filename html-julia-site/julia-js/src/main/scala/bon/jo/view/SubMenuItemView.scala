package bon.jo.view

import bon.jo.SiteModel
import bon.jo.SiteModel.MenuItem
import bon.jo.service.{Raws, SiteService}
import org.scalajs.dom.html.{Div, Image}
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.ExecutionContext
import scala.xml.Node

case class SubMenuItemView(override val menuItem: MenuItem)(implicit override val siteService: SiteService, val siteModelView: SiteModelView)
  extends MenuItemView(menuItem: MenuItem)  with intOnce
    with WithImage[Div, MenuItem]{
  override def id: String = "si-" + menuItem.id

  override def cssClass: String = "sub-menu-item"
  implicit val executionContext : ExecutionContext = siteService.executionContext
  override def imageFor(e: Raws.ImageRawExport): Unit = {
    menuItem.image = siteService.siteModel.allImages.find(_.id == e.id)
    service.update(menuItem).foreach(ee=>{ updateSrc(menuItem.image.get)})

  }

  override def xml(): Node =
    <div id={id}>
      <a class={cssClass} id={"btn-mi-" + id}>
        {menuItem.text}
      </a>{adminXmlOption match {
      case Some(value) => value
      case None =>
    }}{if (value.image.isDefined) {
      <img id={"img-m-" + id} class="oeuvre-img"></img>
    }else{
      <img id={"img-m-" + id} class="oeuvre-img" alt="Choisi moi une image"></img>
    }}

    </div>


  override def init(p: HTMLElement): Unit = {
    super.init(p)
    initImg(p)


  }



  override def factory: Ref[Image] = image.map(_ => Ref[Image]("img-m-" +id)).orNull

  override def image: Option[SiteModel.Image] = value.image
}