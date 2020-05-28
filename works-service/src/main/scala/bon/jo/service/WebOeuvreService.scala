package bon.jo.service

import bon.jo.RawImpl.OeuvreRawExport
import bon.jo.juliasite.model.Schema
import bon.jo.{RawImpl, WebServiceCrud, js}

import scala.concurrent.{ExecutionContext, Future}

trait WebOeuvreService extends OeuvreService with WebServiceCrud[RawImpl.OeuvreRawExport,Int] {

  import dbContext.profile.api._

  implicit val exe: ExecutionContext

  def toOeuvre(m: RawImpl.OeuvreRawExport): Schema.Oeuvre = Schema.Oeuvre(m.id, m.name, m.description, m.dimension.x, m.dimension.y, m.date)

  override def createEntity(m: RawImpl.OeuvreRawExport): Future[Option[RawImpl.OeuvreRawExport]] = {
    val create = oeuvres += toOeuvre(m)

    val linkOeuvre = themesOeuvres += (m.theme, m.id, 0, 0)

    val q = (for {
      cCount <- create

      cOeuvre <- linkOeuvre
    } yield (cCount, cOeuvre))
    val linkImage = m.image.asOption.map(idImg => oeuvreImages += (m.id, idImg))
    val finalQ = run(q).flatMap {
      case (1, 1) =>
        linkImage.map(run).getOrElse(Future.successful(0)).map(e=>{
          Some(m)
        })

      case _ => Future.successful(None)
    }

    finalQ


  }

  def createOeuvreRawExport(o: Schema.Oeuvre, i:Option[ Schema.OeuvresImages], t: Schema.OeuvresThemes): RawImpl.OeuvreRawExport = {
    RawImpl.OeuvreRawExport(o.id, js.BigInt(i.map(_._2)), o.title, RawImpl.DimemsionExport(o.dimensionX, o.dimensionY), o.creation, t._1, o.description)
  }


  def qBase(m: Int): this.dbContext.QueryBaseType = {
    for {
      (oeuvre,oeuvreImage) <- (oeuvres.filter(_.id === m) joinLeft oeuvreImages) on (_.id === _.idOeuvre)
      (oeuvre,themeOeuvre) <- (oeuvres join themesOeuvres) on ( (o,t) => o.id === t.idOeuvre && o.id === oeuvre.id )} yield (oeuvre,oeuvreImage, themeOeuvre)
  }

  def qBase: this.dbContext.QueryBaseType = {
    for {
      (oeuvre,oeuvreImage)<- (oeuvres joinLeft oeuvreImages) on (_.id === _.idOeuvre)
      (oeuvre,themeOeuvre) <- (oeuvres join themesOeuvres) on ( (o,t) => o.id === t.idOeuvre && o.id === oeuvre.id )
    //  t <- themesOeuvres join (o._1.id === t.idOeuvre)
    } yield (oeuvre,oeuvreImage, themeOeuvre)
  }

  def uBase(o: OeuvreRawExport): this.dbContext.UBase = {
    uBase(Schema.Oeuvre(o.id, o.name, o.description, o.dimension.x, o.dimension.y, o.date), o.theme, o.image)
  }

  def uBase(o: Schema.Oeuvre, th: Int, img: js.BigInt): this.dbContext.UBase = {
    for {
      ou <- oeuvres.filter(_.id === o.id).update(o)
      _ <- themesOeuvres.filter(_.idOeuvre === o.id).delete
      ot <- themesOeuvres += (th, o.id, 0, 0)
      _ <- oeuvreImages.filter(_.idOeuvre === o.id).delete
      oi <- img.asOption.map(idImg =>  oeuvreImages += o.id -> idImg).getOrElse(DBIO.successful(0))
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
