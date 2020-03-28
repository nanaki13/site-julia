package bon.jo

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.Services.{themeMapping, themeWithImage}
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

trait MenuService extends WebMenuSevice with RootCreator[RawImpl.ItemRawExport] {

  import dbContext.ctx
  import dbContext.profile.api._

  def crealteImageLink(mOption: Option[RawImpl.ItemRawExport]): Option[FixedSqlAction[Int, NoStream, Write]] = for {
    i <- mOption
    imgId <- i.image.asOption
  } yield {
    dbContext.themeImages += (i.id, imgId)
  }

  def Type(option: Option[Int]): Boolean = option match {
    case Some(value) => true
    case None => false
  }

  def addMenu(t: RawImpl.ItemRawExport): Future[Option[RawImpl.ItemRawExport]] = {

    // (Int, String,Option[Int],Int,Int,Boolean)
    val insert = dbContext.themes += (t.id, t.text, t.parent.asOption, t.x.v, t.y.v, Type(t.parent.asOption))
    run(insert map { case 1 => Some(t); case 0 => None })
  }


  //    def getMenu: Future[Seq[MenuItem]] = dbContext.db.run(dbContext.themes.filter(_.idThemeParent.isEmpty).result) map {
  //      e => {
  //        e.map(i => {
  //          MenuItem(Option.apply(i._1), i._2, None, i._4, i._5, None, "")
  //        })
  //      }
  //    }


  //    def getSubMenu(parentId: Int): Future[Seq[MenuItem]] = {
  //      val select = for {
  //        ((t, _), i) <- dbContext.themes.filter(_.idThemeParent === parentId).joinLeft(dbContext.themeImages).on(_.id === _.idTheme)
  //          .joinLeft(dbContext.images.map(ee => (ee.id, ee.contentType, ee.name))).on(_._2.map(_.idImage) === _._1)
  //      } yield (t, i)
  //
  //      for {
  //        rowResSeq: Seq[((Int, String, Option[Int], Int, Int, Boolean), Option[(Int, String, String)])] <- dbContext.db.run(select.result)
  //      } yield {
  //        for {
  //          rowRes <- rowResSeq
  //          theme: Schema.Themes = rowRes._1
  //          image: Option[ImgLinkOb] = rowRes._2.map { e: (Int, String, String) =>
  //            imageMapping(e)
  //          }
  //        } yield {
  //          themeMapping(theme).copy(image = image)
  //        }
  //      }
  //    }
}


class MenuServiceImpl(val dbContext: RepositoryContext with SiteRepository) (implicit val manifest: Manifest[RawImpl.ItemRawExport]) extends MenuService {
  override def before(implicit executionContext: ExecutionContext, m: Materializer): Option[Route] = None

}

trait WebMenuSevice extends Service[RawImpl.ItemRawExport] with WebServiceCrud[RawImpl.ItemRawExport] {
  self: MenuService =>

  import dbContext.ctx
  import dbContext.profile.api._

  override def createEntity(m: RawImpl.ItemRawExport): Future[Option[RawImpl.ItemRawExport]] = addMenu(m)

  override def readEntity(m: Int): Future[Option[RawImpl.ItemRawExport]] = {
    val select = for {
      ((t, _), i) <- themes.filter(_.id === m).joinLeft(themeImages).on(_.id === _.idTheme)
        .joinLeft(images.map(dbContext.imageWithoutDataProjection)).on(_._2.map(_.idImage) === _._1)
    } yield (t, i)
    run(select.result.headOption).map(e => e.map(themeWithImage _ tupled _))
  }


  def groupBy1(q: Query[(Rep[Int], Rep[Int]), (Int, Int), Seq]) = q.result.map(e =>
    e.groupMap(e => e._1)(a => a._2)
  )

  override def readAll: Future[IterableOnce[RawImpl.ItemRawExport]] = {
    val themesOeuvresQueryRaw = for {
      o <- themesOeuvres
    } yield (o.idTheme, o.idOeuvre)

    val themesOeuvresQuery = themesOeuvresQueryRaw.result.map(e =>
      e.groupMap(e => e._1)(a => a._2)
    )
    val themeImagesQueryRaw = for {
      ti <- themeImages
    } yield (ti.idTheme, ti.idImage)

    val themeImagesQuery = groupBy1(themeImagesQueryRaw)

    val themeThemeQueryRaw = for {
      ti <- themes.filter(_.idThemeParent.isDefined)
    } yield (ti.idThemeParent.get, ti.id)

    val themeThemeQuery = groupBy1(themeThemeQueryRaw)

    val r = dbContext.run(for {
      t <- themes.result.map(_.map(themeMapping))
      tt <- themeThemeQuery
      ti <- themeImagesQuery
      to <- themesOeuvresQuery
    } yield {
      t.map {
        e =>
          val imgOption = ti.get(e.id).map(_.head)
          val child = tt.get(e.id).map(_.toList).getOrElse(Nil)
          val oeuvresc = to.get(e.id).map(_.toList).getOrElse(Nil)
          val cp = e.copy(image = js.BigInt(imgOption)
            , children = child, oeuvres = oeuvresc)

          cp

      }
    })

    r
  }

  override def deleteEntity(m: Int): Future[Boolean] = {
    run(DBIO.sequence(List(themes.filter(_.idThemeParent === m).delete, themeImages.filter(_.idTheme === m).delete,
      themes.filter(_.id === m).delete))).map {
      res => res.sum > 0
    }
  }


  //      def extractRowUpdate(m: MenuItem): (Schema.Themes, Option[Int]) = {
  //        (
  //          (m.id.get, m.title, m.themeKey, m.x, m.y, Type(m.`type`)), {
  //          m.image map (_.id)
  //        }
  //        )
  //      }

  def menuUpdate(m: RawImpl.ItemRawExport): Schema.Themes = (m.id, m.text, m.parent.asOption, m.x.v, m.y.v, Type(m.parent.asOption))

  override def updateEntity(m: RawImpl.ItemRawExport): Future[Option[RawImpl.ItemRawExport]] = {

    val mainUpdateQuery = themes.filter(_.id === m.id).update(menuUpdate(m))
    val deletImageOptionQuery = m.image.asOption.map(idImage => {
      themeImages.filter(row => row.idImage =!= idImage && row.idTheme === m.id).delete
    })
    val createImageLink = m.image.asOption.map(idImage => {
      themeImages += (m.id, idImage)
    })
    val resDelte: Future[Int] = (deletImageOptionQuery map {
      e =>
        dbContext.run(e)
    }).getOrElse(Future.successful(0))
    val resCreate: Future[Int] = (createImageLink map {
      e =>
        dbContext.run(e)
    }).getOrElse(Future.successful(0))
    resDelte flatMap { _ =>
      resCreate
    } flatMap { _ =>
      dbContext.run(mainUpdateQuery)
    } map { _ =>
      Some(m)
    }
  }

  override def ressourceName: String = "menu"

}
