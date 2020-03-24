package bon.jo

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.RawImpl.OeuvreRawExport

import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}

import scala.concurrent.{ExecutionContext, Future}

trait OeuvreService extends Service[RawImpl.OeuvreRawExport] with WebServiceCrud[RawImpl.OeuvreRawExport] with RootCreator[RawImpl.OeuvreRawExport]

class OeuvreServiceImpl(val dbContext: RepositoryContext with SiteRepository)(implicit val executionContext: ExecutionContext) extends OeuvreService with WebOeuvreService {
  override def before(implicit ctx: ExecutionContext, m: Materializer): Option[Route] = None


}

trait WebOeuvreService extends OeuvreService with WebServiceCrud[RawImpl.OeuvreRawExport] {

  import dbContext.profile.api._

  implicit val executionContext: ExecutionContext

  def toOeuvre(m: RawImpl.OeuvreRawExport): Schema.Oeuvre = Schema.Oeuvre(m.id, m.name, m.description, m.dimension.x, m.dimension.y, m.date)

  override def createEntity(m: RawImpl.OeuvreRawExport): Future[Option[RawImpl.OeuvreRawExport]] = {
    val create = oeuvres += toOeuvre(m)
    val linkImage = oeuvreImages += (m.id, m.image)
    val linkOeuvre = themesOeuvres += (m.theme, m.id, 0, 0)

    val q = (for {
      cCount <- create
      cImage <- linkImage
      cOeuvre <- linkOeuvre
    } yield (cCount, cImage, cOeuvre))

    val finalQ = q.map {
      case (1, 1, 1) =>
        Some(m)
      case _ => None
    }

    run(finalQ)


  }

  def createOeuvreRawExport(o: Schema.Oeuvre, i: Schema.OeuvresImages, t: Schema.OeuvresThemes): RawImpl.OeuvreRawExport = {
    RawImpl.OeuvreRawExport(o.id, i._2, o.title, RawImpl.DimemsionExport(o.dimensionX, o.dimensionY), o.creation, t._1, o.description)
  }


  def qBase(m: Int): this.dbContext.QueryBaseType = {
    for {
      o <- oeuvres.filter(_.id === m)
      i <- oeuvreImages if (o.id === i.idOeuvre)
      t <- themesOeuvres if (o.id === t.idOeuvre)
    } yield (o, i, t)
  }

  def qBase: this.dbContext.QueryBaseType = {
    for {
      o <- oeuvres
      i <- oeuvreImages if (o.id === i.idOeuvre)
      t <- themesOeuvres if (o.id === t.idOeuvre)
    } yield (o, i, t)
  }

  def uBase(o: OeuvreRawExport): this.dbContext.UBase = {
    uBase(Schema.Oeuvre(o.id, o.name, o.description, o.dimension.x, o.dimension.y, o.date), o.theme, o.image)
  }

  def uBase(o: Schema.Oeuvre, th: Int, img: Int): this.dbContext.UBase = {
    for {
      ou <- oeuvres.filter(_.id === o.id).update(o)
      _ <- themesOeuvres.filter(_.idOeuvre === o.id).delete
      ot <- themesOeuvres += (th, o.id, 0, 0)
      _ <- oeuvreImages.filter(_.idOeuvre === o.id).delete
      oi <- oeuvreImages += o.id -> img
    } yield {
      ou + ot + oi > 0
    }
  }


  override def readEntity(m: Int): Future[Option[RawImpl.OeuvreRawExport]] = {
    run(qBase(m).result.headOption map {
      case Some(v) => Some(createOeuvreRawExport _ tupled v)
      case None => None
    })

  }


  override def readAll: Future[IterableOnce[RawImpl.OeuvreRawExport]] = {
    run(qBase.result.map(e => e.map(v => createOeuvreRawExport _ tupled v)))
  }

  override def deleteEntity(m: Int): Future[Boolean] = {
    run(for {
      d1 <- oeuvreImages.filter(_.idOeuvre === m).delete
      d2 <- themesOeuvres.filter(_.idOeuvre === m).delete
      d3 <- oeuvres.filter(_.id === m).delete
    } yield {
      d1 + d2 + d3 > 0
    })
  }


  override def updateEntity(m: RawImpl.OeuvreRawExport): Future[Option[RawImpl.OeuvreRawExport]] = {
    run(uBase(m) map {
      case true => Some(m)
      case false => None
    })
  }

  override def ressourceName: String = "oeuvre"
}
