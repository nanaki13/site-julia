package bon.jo.service

import bon.jo.juliasite.model.Schema.{Descri, SiteElement}
import bon.jo.{RawImpl, WebServiceCrud}

import scala.concurrent.{ExecutionContext, Future}


trait WebSiteElementService extends SiteElementService with WebServiceCrud[RawImpl.SiteElementExport,Int] {

  implicit val exe: ExecutionContext

  import dbContext.profile.api._
  //TODO finir et après faire dans la vue(Remplacer getALL image par les images du menu)
  override def createEntity(m: RawImpl.SiteElementExport): Future[Option[RawImpl.SiteElementExport]] = ???

  override def readEntity(m: Int): Future[Option[RawImpl.SiteElementExport]] = ???

  override def readAll: Future[IterableOnce[RawImpl.SiteElementExport]] = ???

  override def deleteEntity(m: Int): Future[Boolean] = ???

  override def updateEntity(m: RawImpl.SiteElementExport): Future[Option[RawImpl.SiteElementExport]] = ???

  def imagesMenuLnk(): Future[Seq[(Int, String)]] = {
    db.run((for {
      se <- dbContext.siteElement if se.descriminator === Descri.IMAGE_MENU
      im <- images if im.id === se.imageKey
    } yield (im.id, im.contentType)).result)
  }
  def addSiteElement(imageKey: Option[Int], desc: Int,order : Int): Future[Option[Int]] = {
    val se = SiteElement(0, imageKey, desc,order)
    val add = (dbContext.siteElement += se).flatMap(
      _ => dbContext.siteElement.sortBy(_.id.desc).map(e => e.id).result.headOption
    )
    db.run(add.map {
      case Some(1) => imageKey
      case _ => None
    })
  }

  override def ressourceName: String = "site-element"
}
