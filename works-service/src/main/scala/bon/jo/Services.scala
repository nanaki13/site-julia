package bon.jo

import java.time.LocalDate

import bon.jo.Services.OeuvreService.OeuvreAndPosition
import bon.jo.SiteModel.{ImgLink, ImgLinkOb, MenuItem, OkResponse}
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import slick.dbio.Effect.Write
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.TableQuery
import slick.sql.FixedSqlAction

import scala.collection.compat.Factory
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object Services {

  implicit val d: Factory[Int, List[Int]] = new Factory[Int, List[Int]]() {
    override def fromSpecific(it: IterableOnce[Int]): List[Int] = it.iterator.to(List)

    override def newBuilder: mutable.Builder[Int, List[Int]] = new mutable.ListBuffer[Int]()
  }
  def themeMapping(themeDB: Schema.Themes): MenuItem = {
    MenuItem(Some(themeDB._1), themeDB._2, themeDB._3, themeDB._4, themeDB._5, None, Type(themeDB._6))
  }

  def themeWithImage(themeDB: Schema.Themes, imgDb: Option[Schema.ImagesWithoutData]): MenuItem = {
    themeMapping(themeDB).copy(image = imgDb.map(imageMapping))
  }

  def imageMapping(imgDb: Schema.ImagesWithoutData): ImgLinkOb = {
    ImgLinkOb(imgDb._1, imgDb._2, ImgLink(imgDb._1, imgDb._2), imgDb._3)
  }


  trait MenuService extends Service[MenuItem] {

    import dbContext.profile.api._

    def crealteImageLink(mOption: Option[MenuItem]): Option[FixedSqlAction[Int, NoStream, Write]] = for {
      i <- mOption
      iId <- i.id
      img <- i.image
    } yield {
      val imgId = img.id
      dbContext.themeImages += (iId, imgId)
    }

    def addMenu(t: MenuItem): Future[Option[MenuItem]] = {


      val insert = dbContext.themes += (0, t.title, t.themeKey, t.x, t.y, Type(t.`type`))
      dbContext.db.run(insert) flatMap (_ => {
        dbContext.db.run(dbContext.themes.sortBy(_.id.desc).result.headOption.map {
          case Some(tuple) => Some(MenuItem(Some(tuple._1), tuple._2, tuple._3, tuple._4, tuple._5, t.image, t.`type`))
          case _ => None
        } map { e => {
          val createLink = crealteImageLink(e)
          createLink map {
            e => dbContext.db.run(e)
          }
          e
        }
        })
      })
    }


    def getMenu: Future[Seq[MenuItem]] = dbContext.db.run(dbContext.themes.filter(_.idThemeParent.isEmpty).result) map {
      e => {
        e.map(i => {
          MenuItem(Option.apply(i._1), i._2, None, i._4, i._5, None, "")
        })
      }
    }


    def getSubMenu(parentId: Int): Future[Seq[MenuItem]] = {
      val select = for {
        ((t, _), i) <- dbContext.themes.filter(_.idThemeParent === parentId).joinLeft(dbContext.themeImages).on(_.id === _.idTheme)
          .joinLeft(dbContext.images.map(ee => (ee.id, ee.contentType, ee.name))).on(_._2.map(_.idImage) === _._1)
      } yield (t, i)

      for {
        rowResSeq: Seq[((Int, String, Option[Int], Int, Int, Boolean), Option[(Int, String, String)])] <- dbContext.db.run(select.result)
      } yield {
        for {
          rowRes <- rowResSeq
          theme: Schema.Themes = rowRes._1
          image: Option[ImgLinkOb] = rowRes._2.map { e: (Int, String, String) =>
            imageMapping(e)
          }
        } yield {
          themeMapping(theme).copy(image = image)
        }
      }
    }
  }

  def Type(boolean: Boolean): String = {
    if (boolean) {
      "page"
    } else {
      "subMenu"
    }
  }

  def Type(txt: String): Boolean = {
    txt match {
      case "page" => true

      case _ => false

    }
  }


  trait Service[WebMessage <: OkResponse] {
    implicit val ctx: ExecutionContext

    val dbContext: RepositoryContext with SiteRepository

    def oeuvres = dbContext.oeuvres

    def images: TableQuery[dbContext.ImagesTable] = dbContext.images


    def themes = dbContext.themes

    def oeuvreImages = dbContext.oeuvreImages

    def themesOeuvres = dbContext.themesOeuvres

    def themeImages = dbContext.themeImages

    def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = dbContext.run(a)

  }

  trait WebServiceCrud[WebMessage <: OkResponse] {

    self: Service[WebMessage] =>

    def createEntity(m: WebMessage): Future[Option[WebMessage]]

    def readEntity(m: Int): Future[Option[WebMessage]]

    def readAll: Future[IterableOnce[WebMessage]]

    def deleteEntity(m: Int): Future[Boolean]

    def updateEntity(m: WebMessage): Future[Option[WebMessage]]

    def ressourceName: String
  }


  object OeuvreService {

    object OeuvreAndPosition {
      def apply(o: Schema.Oeuvre, x: Int = 0, y: Int = 0, image: Option[ImgLinkOb] = None, tk: Option[Int] = None): OeuvreAndPosition = OeuvreAndPosition(o.id,
        o.title,
        o.description,
        o.dimensionX,
        o.dimensionY,
        o.creation,
        x,
        y,
        image,
        tk)
    }

    case class OeuvreAndPosition(id: Int, title: String, description: String, dimensionX: Float, dimensionY: Float,
                                 creation: Int, x: Int, y: Int, image: Option[ImgLinkOb], themeKey: Option[Int]) extends OkResponse {
      def toOeuvre: Schema.Oeuvre = Schema.Oeuvre(id, title, description, dimensionX, dimensionY, creation)
    }


  }

  trait OeuvreService extends Service[OeuvreAndPosition] with WebServiceCrud[OeuvreAndPosition]

  trait ServiceFactory {
    def dbContext: RepositoryContext with SiteRepository

    val _dbContext: RepositoryContext with SiteRepository = dbContext

    import _dbContext.profile.api._

    trait WebImageSevice extends ImageService with WebServiceCrud[ImgLinkOb] {
      override def createEntity(m: ImgLinkOb): Future[Option[ImgLinkOb]] = saveImage(Some(new Array[Byte](0)), m.contentType, m.name).map(e => {
        e map { ee =>
          m.copy(ee._1)
        }
      })


      override def readEntity(m: Int): Future[Option[ImgLinkOb]] = {
        val selectAndMap = dbContext.images.filter(_.id === m).map(t => (t.id, t.contentType, t.name)).result.headOption.map(ee =>
          ee map { e =>
            ImgLinkOb(e._1, e._2, ImgLink(e._1, e._2), e._3)
          }
        )
        dbContext.db.run(selectAndMap)
      }

      override def readAll: Future[IterableOnce[ImgLinkOb]] = {
        val selectAndMap = dbContext.images.map(t => (t.id, t.contentType, t.name)).result.map(ee =>
          ee map { e =>
            ImgLinkOb(e._1, e._2, ImgLink(e._1, e._2), e._3)
          }
        )
        dbContext.db.run(selectAndMap)
      }

      override def deleteEntity(m: Int): Future[Boolean] = {
        val deleteAction = dbContext.images.filter(_.id === m).delete
        run(deleteAction).map(e => e == 1)
      }

      override def ressourceName: String = "image"

      override val dbContext = _dbContext
    }

    trait WebOeuvreService extends OeuvreService with WebServiceCrud[OeuvreAndPosition] {
      override val dbContext = _dbContext

      def getOeuvres(parentId: Int): Future[Seq[OeuvreAndPosition]] = {
        val oAndPos = oeuvres join themesOeuvres.filter(_.idTheme === parentId) on (_.id === _.idOeuvre)
        val relationImage = oeuvreImages.sortBy(_.idOeuvre.desc)
        val img = images map dbContext.imageWithoutDataProjection
        val oeurvreAndImgRelation = oAndPos joinLeft relationImage on (_._1.id === _.idOeuvre)
        val join2 = oeurvreAndImgRelation joinLeft img on (_._2.map(_.idImage) === _._1)

        val s = for {
          ((o, _), i) <- join2
        } yield (o, i)
        run(s.result).map(e => e.map(toOeuvreWithTheme _ tupled _))
      }

      override def createEntity(m: OeuvreAndPosition): Future[Option[OeuvreAndPosition]] = {
        val create = oeuvres += m.toOeuvre
        run(create) flatMap {
          cnt =>
            if (cnt == 1) {
              val findId = oeuvres sortBy (_.id.desc) filter (_.title === m.title) map (_.id)
              val createRes: Future[Option[OeuvreAndPosition]] = run(findId.result.headOption) map {
                id =>
                  id.map(e => m.copy(id = e))

              }
              val creteLink: Int => Option[FixedSqlAction[Int, NoStream, Write]] = (oId: Int) => m.image map {
                i =>
                  oeuvreImages += (oId, i.id)
              }
              val createThemeLink: Int => Option[Future[Boolean]] = (oId: Int) => for {
                tk <- m.themeKey

              } yield {
                run {
                  for {
                    op <- themesOeuvres += (tk, oId, m.x, m.y)
                  } yield {
                    op == 1
                  }
                }

              }
              createRes flatMap { miO => {
                miO match {
                  case Some(mi) => {
                    val l = creteLink(mi.id) map run match {
                      case Some(v) => v
                      case _ => Future.successful(0)
                    }
                    l flatMap { _ =>
                      createThemeLink(mi.id) match {
                        case Some(v) => v.map(_ => miO)
                        case _ => Future.successful(miO)
                      }
                    }
                  }
                  case e => Future.successful(e)
                }
              }
              }
            } else {
              Future.successful(None)
            }
        }

      }

      def toOeuvreWithTheme(ot: (Schema.Oeuvre, Schema.OeuvresThemes), img: Option[Schema.ImagesWithoutData]): OeuvreAndPosition = {
        OeuvreAndPosition(ot._1, ot._2._3, ot._2._4, img map imageMapping, Some(ot._2._1))
      }

      def toOeuvre(o: Schema.Oeuvre, img: Option[Schema.ImagesWithoutData]): OeuvreAndPosition = {
        OeuvreAndPosition(o, 0, 0, img map imageMapping, None)
      }

      override def readEntity(m: Int): Future[Option[OeuvreAndPosition]] = {
        val oeuvreBase = oeuvres.filter(_.id === m)
        val relationImage = oeuvreImages.sortBy(_.idOeuvre.desc)
        val img = images map dbContext.imageWithoutDataProjection
        val oeurvreAndImgRelation = oeuvreBase joinLeft relationImage on (_.id === _.idOeuvre)
        val join2 = oeurvreAndImgRelation joinLeft img on (_._2.map(_.idImage) === _._1)

        val s = for {
          ((o, _), i) <- join2
        } yield (o, i)
        run(s.result.headOption).map(e => e.map(toOeuvre _ tupled _))
      }

      override def readAll: Future[IterableOnce[OeuvreAndPosition]] = {
        val oeuvreBase = oeuvres
        val relationImage = oeuvreImages.sortBy(_.idOeuvre.desc)
        val img = images map dbContext.imageWithoutDataProjection
        val oeurvreAndImgRelation = oeuvreBase joinLeft relationImage on (_.id === _.idOeuvre)
        val join2 = oeurvreAndImgRelation joinLeft img on (_._2.map(_.idImage) === _._1)

        val s = for {
          ((o, _), i) <- join2
        } yield (o, i)
        run(s.result).map(e => e.map(toOeuvre _ tupled _))
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

      def extractRowUpdate(m: OeuvreAndPosition): (Schema.Oeuvre, Option[Int]) = {
        val o = m.toOeuvre
        (
          o, {
          m.image map (_.id)
        }
        )
      }


      override def updateEntity(m: OeuvreAndPosition): Future[Option[OeuvreAndPosition]] = {
        val mi: Future[Int] = for {
          o <- run(oeuvres.filter(_.id === m.id).update(m.toOeuvre))
          ot <- m.themeKey match {
            case Some(v) => run(DBIO.sequence(List(
              themesOeuvres.filter(_.idOeuvre === m.id).delete,
              themesOeuvres += (v,m.id,m.x,m.y)
            ))).map(e=>e.sum)
            case _ => Future.successful(0)
          }
          oi <- m.image match {
            case Some(v) => run(DBIO.sequence(List(
              oeuvreImages.filter(_.idOeuvre === m.id).delete,
              oeuvreImages += (m.id,v.id)
            ))).map(e=>e.sum)
            case _ => Future.successful(0)
          }
        } yield {
          o + oi +ot
        }
        mi.map {
          e =>
            if (e > 0) {
              Some(m)
            } else {
              None
            }
        }

      }

      override def ressourceName: String = "oeuvre"
    }

    trait WebMenuSevice extends MenuService with WebServiceCrud[MenuItem] {
      override def createEntity(m: MenuItem): Future[Option[MenuItem]] = addMenu(m)

      override def readEntity(m: Int): Future[Option[MenuItem]] = {
        val select = for {
          ((t, _), i) <- themes.filter(_.id === m).joinLeft(themeImages).on(_.id === _.idTheme)
            .joinLeft(images.map(dbContext.imageWithoutDataProjection)).on(_._2.map(_.idImage) === _._1)
        } yield (t, i)
        run(select.result.headOption).map(e => e.map(themeWithImage _ tupled _))
      }

      override def readAll: Future[IterableOnce[MenuItem]] = {
        val select = for {
          ((t, _), i) <- themes.joinLeft(themeImages).on(_.id === _.idTheme)
            .joinLeft(images.map(dbContext.imageWithoutDataProjection)).on(_._2.map(_.idImage) === _._1)
        } yield (t, i)
        run(select.result).map(e => e.map(themeWithImage _ tupled _))
      }



      override def deleteEntity(m: Int): Future[Boolean] = {
        run(DBIO.sequence(List(   themes.filter(_.idThemeParent === m).delete, themeImages.filter(_.idTheme === m).delete,
          themes.filter(_.id === m).delete))).map {
          res => res.sum > 0
        }
      }


      def extractRowUpdate(m: MenuItem): (Schema.Themes, Option[Int]) = {
        (
          (m.id.get, m.title, m.themeKey, m.x, m.y, Type(m.`type`)), {
          m.image map (_.id)
        }
        )
      }

      def menuUpdate(m: MenuItem): Schema.Themes = (m.id.get, m.title, m.themeKey, m.x, m.y, Type(m.`type`))

      override def updateEntity(m: MenuItem): Future[Option[MenuItem]] = {
        val mi = for {

          t <- run(themes.filter(_.id === m.id).update(menuUpdate(m)))
          oi <- m.image match {
            case Some(v) => run(DBIO.sequence(List(
              themeImages.filter(_.idTheme === m.id).delete,
              themeImages += m.id.get -> v.id
            ))).map(e=>e.sum)
            case _ => Future.successful(0)
          }
        } yield {
           t + oi
        }
        mi.map {
          e =>
            if (e > 0) {
              Some(m)
            } else {
              None
            }
        }
      }

      override def ressourceName: String = "menu"

      override val dbContext: RepositoryContext with SiteRepository = _dbContext
    }

  }

  trait ImageService extends Service[ImgLinkOb] {


    import dbContext.profile.api._

    def addImagesMenu(b: Array[Byte], contentType: String, name: String): Future[Option[(Int, String)]] = dbContext.addImagesMenu(b, contentType, name)

    /**
      *
      * @param byttes
      * @param contentType
      * @return image id and contentType
      */
    def saveImage(byttes: Option[Array[Byte]], contentType: String, name: String): Future[Option[(Int, String)]] = {
      val sbInsert: FixedSqlAction[Int, NoStream, Write] = dbContext.images += (0, contentType, byttes.get, name)

      run(sbInsert) flatMap {
        _ => {
          run(dbContext.images.filter(_.name === name).map(e => (e.id, e.contentType)).sortBy(_._1.desc).result.headOption)
        }
      }
    }

    def updateEntity(forPatch: ImgLinkOb): Future[Option[ImgLinkOb]] = {
      val update = dbContext.images.filter(_.id === forPatch.id).map(_.name).update(forPatch.name) map { i =>
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

    def deleteImage(id: Int): Future[Boolean] = dbContext.deleteImage(id)

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

}


