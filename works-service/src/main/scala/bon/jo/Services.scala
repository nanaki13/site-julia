package bon.jo

import bon.jo.SiteModel.{ImgLink, ImgLinkOb, MenuItem}
import bon.jo.juliasite.model.{Images, Oeuvre}
import bon.jo.juliasite.pers.{PostgresRepo, RepositoryContext, SiteRepository}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

object Services {

  class SericeImpl(override val dbConntext: RepositoryContext with SiteRepository) extends Service {
    override implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  }

  trait MenuService extends Service {

    import dbConntext.profile.api._

    def crealteImageLink(mOption: Option[MenuItem]): Option[FixedSqlAction[Int, NoStream, Write]] = for {
      i <- mOption
      iId <- i.id
      img <- i.image
    } yield {
      val imgId = img.id
      dbConntext.themeImages += (iId, imgId)
    }

    def addMenu(t: MenuItem): Future[Option[MenuItem]] = {


      val finalTheme : Boolean = t.`type` match {
        case  "page" => true
        case _ => false
      }
      val insert = dbConntext.themes += (0, t.title, t.themeKey, t.x, t.y, finalTheme)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem(Some(tuple._1), tuple._2, tuple._3, tuple._4, tuple._5, t.image, t.`type`))
          case _ => None
        } map { e => {
          val createLink = crealteImageLink(e)
          createLink map {
            e => dbConntext.db.run(e)
          }
          e
        }
        })
      })
    }


    def getMenu: Future[Seq[MenuItem]] = dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent.isEmpty).result) map {
      e => {
        e.map(i => {
          MenuItem(Option.apply(i._1), i._2, None, i._4, i._5,None,"")
        })
      }
    }

    def getSubMenu(parentId: Int): Future[Seq[MenuItem]] = {
      val select = for{
        ((t,_),i) <- dbConntext.themes.filter(_.idThemeParent === parentId).joinLeft(dbConntext.themeImages).on(_.id === _.idTheme)
          .joinLeft(dbConntext.images.map(ee => (ee.id, ee.contentType, ee.name))).on(_._2.map(_.idImage) === _._1)
      } yield (t,i)

      for{
        rowResSeq: Seq[((Int, String, Option[Int], Int, Int), Option[(Int, String, String)])] <- dbConntext.db.run(select.result)
      } yield {
        for {
          rowRes <- rowResSeq
          theme: (Int, String, Option[Int], Int, Int, Boolean) = rowRes._1
          image: Option[ImgLinkOb] = rowRes._2.map{e : (Int,String,String) =>
            ImgLinkOb(e._1,e._2, ImgLink(e._1,e._2), e._3)
          }
        } yield {
          MenuItem(Option.apply(theme._1), theme._2, Some(parentId), theme._4, theme._5,image,if{ theme._6})
        }
      }
    }
  }
  def Type(boolean: Boolean): String ={
    if(boolean){

    }else{

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

    def addImagesMenu(b: Array[Byte], contentType: String, name: String): Future[Option[(Int, String)]] = dbConntext.addImagesMenu(b, contentType, name)

    /**
      *
      * @param byttes
      * @param contentType
      * @return image id and contentType
      */
    def saveImage(byttes: Option[Array[Byte]], contentType: String, name: String): Future[Option[(Int, String)]] = {
      val sbInsert: FixedSqlAction[Int, NoStream, Write] = dbConntext.images += Images(0, contentType, byttes.get, name)

      dbConntext.db.run(sbInsert) flatMap {
        _ => {
          dbConntext.db.run(dbConntext.images.filter(_.name === name).map(e => (e.id, e.contentType)).sortBy(_._1.desc).result.headOption)
        }
      }
    }

    def update(forPatch: ImgLinkOb): Future[Option[ImgLinkOb]] = {
      val update = dbConntext.images.filter(_.id === forPatch.id).map(_.name).update(forPatch.name) map { i =>
        if (i == 1) {
          Some(forPatch)
        } else {
          None
        }
      }
      dbConntext.db.run(update)
    }

    def getImage(id: String): Future[Option[(Array[Byte], String)]] = {
      dbConntext.db.run(dbConntext.images.filter(_.id === Integer.parseInt(id)).map(e => (e.imgData, e.contentType))
        .result.headOption)
    }

    def deleteImage(id: Int): Future[Boolean] = dbConntext.deleteImage(id)

    //    def imageMenuLink(): Future[Seq[ImgLinkOb]] = {
    //      dbConntext.imagesMenuLnk().map(l => l.map(e => ImgLinkOb(ImgLink(e._1, e._2))))
    //    }

    def imagesLink(): Future[Seq[ImgLinkOb]] = {
      dbConntext.db.run {
        dbConntext.images.map(e => (e.id, e.contentType, e.name)).result
          .map(l => l.map(e =>
            ImgLinkOb(e._1, e._2, ImgLink(e._1, e._2), e._3)))
      }

    }

    def addRootTheme(theme: MenuItem): Future[Option[MenuItem]] = {
      val insert = dbConntext.themes += (0, theme.title, None, theme.x, theme.y)
      dbConntext.db.run(insert) flatMap (_ => {
        dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem(Some(tuple._1), tuple._2, tuple._3, tuple._4, tuple._5))
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


