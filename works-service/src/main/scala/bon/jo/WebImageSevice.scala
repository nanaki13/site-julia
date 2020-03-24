package bon.jo

import bon.jo.SiteModel.{ImgLink, ImgLinkOb}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.Future

trait WebImageSevice extends ImageService {


  import dbContext.ctx
  import dbContext.profile.api._

  val contentTypeIs: PartialFunction[String, String] = {
    case "jpg" => "image/jpeg"
    case "png" => "image/png"
    case "svg" => "image/svg+xml"
  }


  def asOption[P, R](s: P)(contentTypePartial: PartialFunction[P, R]): Option[P] = Some(s).filter(contentTypePartial.isDefinedAt)


  def contentType(str: String): String = {
    str match {
      case contentTypeIs(_this) => _this
      case _ => "inconnu"
    }

  }

  override def createEntity(m: RawImpl.ImageRawExport): Future[Option[RawImpl.ImageRawExport]] =
    saveImage(Some(new Array[Byte](0)), m.id, contentType(m.link.substring(m.link.lastIndexOf('.') + 1)),
      m.link.substring(0, m.link.lastIndexOf('.')), m.base).map(e => {
      e map { ee =>
        m.copy(ee._1)
      }
    })


  override def readEntity(m: Int): Future[Option[RawImpl.ImageRawExport]] = {
    val selectAndMap = images.filter(_.id === m).map(t => (t.id, t.contentType, t.name, t.base)).result.headOption.map(ee =>
      ee map { e =>
        RawImpl.ImageRawExport(e._1, e._1 + "." + e._2.substring(e._2.lastIndexOf('/') + 1), e._4)
      }
    )
    db.run(selectAndMap)
  }

  override def readAll: Future[IterableOnce[RawImpl.ImageRawExport]] = {
    val selectAndMap = images.map(t => (t.id, t.contentType, t.name, t.base)).result.map(ee =>
      ee map { e =>
        RawImpl.ImageRawExport(e._1, e._1 + "." + e._2.substring(e._2.lastIndexOf('/') + 1), e._4)
      }
    )
    db.run(selectAndMap)
  }

  override def deleteEntity(m: Int): Future[Boolean] = {
    val deleteAction = images.filter(_.id === m).delete
    run(deleteAction).map(e => e == 1)
  }

  override def ressourceName: String = "image"


  /**
   *
   * @param byttes
   * @param contentType
   * @return image id and contentType
   */
  def saveImage(byttes: Option[Array[Byte]], id: Int, contentType: String, name: String, base: String): Future[Option[(Int, String, String, String)]] = {
    val sbInsert: FixedSqlAction[Int, NoStream, Write] = dbContext.images += (id, contentType, byttes.get, name, base: String)

    run(sbInsert) map { case 1 => Some(id, contentType, name, base: String) }
  }

  def updateEntity(forPatch: RawImpl.ImageRawExport): Future[Option[RawImpl.ImageRawExport]] = {
    val update = dbContext.images.filter(_.id === forPatch.id).map(_.name).update(forPatch.link) map { i =>
      if (i == 1) {
        Some(forPatch)
      } else {
        None
      }
    }
    run(update)
  }

  def getImage(id: String): Future[Option[(Array[Byte], String)]] = {
    run(dbContext.images.filter(_.id === Integer.parseInt(id)).map(e => (e.imgData, e.contentType))
      .result.headOption)
  }


  //    def imageMenuLink(): Future[Seq[ImgLinkOb]] = {
  //      dbContext.imagesMenuLnk().map(l => l.map(e => ImgLinkOb(ImgLink(e._1, e._2))))
  //    }

  def imagesLink(): Future[Seq[ImgLinkOb]] = {
    run {
      dbContext.images.map(e => (e.id, e.contentType, e.name)).result
        .map(l => l.map(e =>
          ImgLinkOb(e._1, e._2, ImgLink(e._1, e._2), e._3)))
    }

  }
}
