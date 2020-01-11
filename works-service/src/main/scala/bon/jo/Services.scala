package bon.jo

import bon.jo.SiteModel.{ImgLink, ImgLinkOb, MenuItem}
import bon.jo.juliasite.model.{Images, Oeuvre}
import bon.jo.juliasite.pers.{PostgresRepo, RepositoryContext, SiteRepository}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

object Services {

  class SericeImpl(override val dbConntext: RepositoryContext with SiteRepository) extends Service {
    override implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  }

  trait MenuService extends Service {

    import dbConntext.profile.api._

    def addMenu(t: MenuItem): Future[Option[MenuItem]] = {

      val insert = dbConntext.themes += (0, t.title, t.themeKey, t.x, t.y)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem(Some(tuple._1), tuple._2, tuple._3, tuple._4, tuple._5))
          case _ => None
        })
      })
    }


    def getMenu: Future[Seq[MenuItem]] = dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent.isEmpty).result) map {
      e => {
        e.map(i => {
          MenuItem(Option.apply(i._1), i._2, None, i._4, i._5)
        })
      }
    }

    def getSubMenu(parentId: Int): Future[Seq[MenuItem]] = {
      dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent === parentId).result) map {
        l => l.map(i => MenuItem(Option.apply(i._1), i._2, Some(parentId), i._4, i._5))
      }
    }

  }

  trait Service {
    implicit val ctx: ExecutionContext

    val dbConntext: RepositoryContext with SiteRepository

  }

  object OeuvreService {

    case class OeuvreAndPosition(oeuvre: Oeuvre, x: Int, y: Int)

  }

  trait OeuvreService extends Service {


    import OeuvreService._
    import dbConntext.profile.api._

    def getOeuvres(parentId: Int): Future[Seq[OeuvreAndPosition]] = {
      dbConntext.db.run(dbConntext.getOuevresByTheme(parentId).result.map(e => e.map(OeuvreAndPosition.tupled)))
    }
  }

  trait ImageService extends Service {


    import dbConntext.profile.api._

    def addImagesMenu(b: Array[Byte], contentType: String): Future[Option[(Int, String)]] = dbConntext.addImagesMenu(b, contentType)

    /**
      *
      * @param byttes
      * @param contentType
      * @return image id and contentType
      */
    def saveImage(byttes: Option[Array[Byte]], contentType: String): Future[Option[(Int, String)]] = {
      val sbInsert: FixedSqlAction[Int, NoStream, Write] = dbConntext.images += Images(0, contentType, byttes.get)

      dbConntext.db.run(sbInsert) flatMap {
        _ => {
          dbConntext.db.run(dbConntext.images.map(e => (e.id, e.contentType)).sortBy(_._1.desc).result.headOption)
        }
      }
    }

    def getImage(id: String): Future[Option[(Array[Byte], String)]] = {
      dbConntext.db.run(dbConntext.images.filter(_.id === Integer.parseInt(id)).map(e => (e.imgData, e.contentType)).result.headOption)
    }

    def imageMenuLink(): Future[Seq[ImgLinkOb]] = {
      dbConntext.imagesMenuLnk().map(l => l.map(e => ImgLinkOb(ImgLink(e._1, e._2))))
    }

    def addRootTheme(theme: MenuItem): Future[Option[MenuItem]] = {
      val insert = dbConntext.themes += (0, theme.title, None,theme.x,theme.y)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem(Some(tuple._1), tuple._2, tuple._3,tuple._4,tuple._5))
          case _ => None
        })
      })

    }
  }


  def imageService(implicit ctxp: ExecutionContext): ImageService = {
    new ImageService {
      override implicit val ctx: ExecutionContext = ctxp

      override val dbConntext: RepositoryContext with SiteRepository = PostgresRepo
    }
  }

  def menuService(implicit ctxp: ExecutionContext): MenuService = {
    new MenuService {
      override implicit val ctx: ExecutionContext = ctxp

      override val dbConntext: RepositoryContext with SiteRepository = PostgresRepo
    }
  }

}


