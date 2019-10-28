package controllers.services

import bon.jo.helloworld.juliasite.model.Images
import bon.jo.helloworld.juliasite.pers.{RepositoryContext, SiteRepository}
import controllers.SiteModel.{ImgLink, ImgLinkOb, MenuItem}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

object Services{
  class SericeImpl(override val dbConntext: RepositoryContext with SiteRepository) extends Service {
    override implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  }

  trait MenuService extends Service {
    val dbc = dbConntext

    import dbc.profile.api._

    def addChildTheme(t: MenuItem): Future[Option[MenuItem]] = {
      val insert = dbConntext.themes += (0, t.title, t.parentTheme)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem.apply((Some(tuple._1),tuple._2,tuple._3)))
          case _ => None
        })
      })
    }


    def getMenu: Future[Seq[MenuItem]] = dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent.isEmpty).result) map {
      e => {
        e.map(i => {
          MenuItem(Option.apply(i._1), i._2, None)
        })
      }
    }

    def getSubMenu(parentId: Int): Future[Seq[MenuItem]] = {
      dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent === parentId).result) map {
        l => l.map(i => MenuItem(Option.apply(i._1), i._2, Some(parentId)))
      }
    }

  }

  trait Service {
    implicit val ctx: ExecutionContext

    def dbConntext: RepositoryContext with SiteRepository
  }

  trait ImageService extends Service {


    val dbc = dbConntext

    def addImagesMenu(b: Array[Byte], contentType: String): Future[Option[(Int, String)]] = dbConntext.addImagesMenu(b, contentType)

    import dbc.profile.api._

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
      val insert = dbConntext.themes += (0, theme.title, None)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple)  => Some(MenuItem.apply((Some(tuple._1),tuple._2,tuple._3)))
          case _ => None
        })
      })

    }
  }
}
