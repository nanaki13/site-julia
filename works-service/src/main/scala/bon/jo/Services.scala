package bon.jo

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

    def oeuvresThemes = dbContext.oeuvresThemes

    def themeImages = dbContext.themeImages

    def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = dbContext.run(a)

  }

  trait WebServiceCrud[WebMessage <: OkResponse] {

    self: Service[WebMessage] =>

    def create(m: WebMessage): Future[Option[WebMessage]]

    def read(m: Int): Future[Option[WebMessage]]

    def readAll: Future[IterableOnce[WebMessage]]

    def delete(m: Int): Future[Boolean]

    def update(m: WebMessage): Future[Option[WebMessage]]

    def ressourceName: String
  }


  object OeuvreService {

    case class OeuvreAndPosition(oeuvre: Schema.Oeuvre, x: Int, y: Int, image: Option[ImgLinkOb]) extends OkResponse

  }

  trait OeuvreService extends Service[OeuvreAndPosition] {


    import OeuvreService._

    def getOeuvres(parentId: Int): Future[Seq[OeuvreAndPosition]] = ???
  }

  trait ServiceFactory {
    def dbContext: RepositoryContext with SiteRepository

    val _dbContext: RepositoryContext with SiteRepository = dbContext

    import _dbContext.profile.api._

    trait WebImageSevice extends ImageService with WebServiceCrud[ImgLinkOb] {
      override def create(m: ImgLinkOb): Future[Option[ImgLinkOb]] = saveImage(Some(new Array[Byte](0)), m.contentType, m.name).map(e => {
        e map { ee =>
          m.copy(ee._1)
        }
      })


      override def read(m: Int): Future[Option[ImgLinkOb]] = {
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

      override def delete(m: Int): Future[Boolean] = {
        val deleteAction = dbContext.images.filter(_.id === m).delete
        run(deleteAction).map(e => e == 1)
      }

      override def ressourceName: String = "image"

      override val dbContext = _dbContext
    }

    trait WebOeuvreService extends OeuvreService with WebServiceCrud[OeuvreAndPosition] {
      override val dbContext = _dbContext
      override def create(m: OeuvreAndPosition): Future[Option[OeuvreAndPosition]] = {
        val create = oeuvres += m.oeuvre
        run(create) flatMap {
          cnt =>
            if (cnt == 1) {
              val findId = oeuvres sortBy (_.id.desc) filter (_.title === m.oeuvre.title) map (_.id)
              val createRes: Future[Option[OeuvreAndPosition]] = run(findId.result.headOption) map {
                id =>
                  id.map(e => m.copy(oeuvre = m.oeuvre.copy(id = e)))

              }
              val creteLink: Int => Option[FixedSqlAction[Int, NoStream, Write]] = (imageId: Int) => m.image map {
                i =>
                  oeuvreImages += (imageId, i.id)
              }
              createRes map { miO => {
                miO flatMap { mi =>
                  creteLink(mi.oeuvre.id) map run
                }
                miO
              }
              }
            } else {
              Future.successful(None)
            }
        }

      }

      def toOeuvre(o: Schema.Oeuvre, img: Option[Schema.ImagesWithoutData]): OeuvreAndPosition = {
        OeuvreAndPosition(o, 0, 0, img map imageMapping)
      }

      override def read(m: Int): Future[Option[OeuvreAndPosition]] = {
        val oeuvreBase = oeuvres.filter(_.id === m)
        val relationImage = oeuvreImages.sortBy(_.idOeuvre.desc)
        val img = images map dbContext.imageWithoutDataProjection
        val oeurvreAndImgRelation = oeuvreBase joinLeft relationImage on (_.id === _.idOeuvre)
        val join2 = oeurvreAndImgRelation joinLeft img on (_._2.map(_.idImage) === _._1)

        val s = for {
          ((o, _), i) <- join2
        } yield (o, i)
        run(s.result.headOption).map(e => e.map(toOeuvre _ tupled _ ))
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

      override def delete(m: Int): Future[Boolean] = {
        run(for {
          d1 <- oeuvreImages.filter(_.idOeuvre === m).delete
          d2 <- oeuvresThemes.filter(_.idOeuvre === m).delete
          d3 <- oeuvres.filter(_.id === m).delete
        } yield {
          d1 + d2 + d3 > 0
        })
      }

      def extractRowUpdate(m: OeuvreAndPosition): (Schema.Oeuvre, Option[Int]) = {
        val o = m.oeuvre
        (
          o, {
          m.image map (_.id)
        }
        )
      }

      override def update(m: OeuvreAndPosition): Future[Option[OeuvreAndPosition]] = {
        val mi = for {
          (o, oi) <- oeuvres.joinLeft(oeuvreImages).on(_.id === _.idImage)
        } yield (o, oi.map(_.idImage))
        run(mi.update(extractRowUpdate(m))) map {
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
      override def create(m: MenuItem): Future[Option[MenuItem]] = addMenu(m)

      override def read(m: Int): Future[Option[MenuItem]] = {
        val select = for {
          ((t, _), i) <- themes.filter(_.id === m).joinLeft(themeImages).on(_.id === _.idTheme)
            .joinLeft(images.map(dbContext.imageWithoutDataProjection)).on(_._2.map(_.idImage) === _._1)
        } yield (t, i)
        run(select.result.headOption).map(e => e.map(themeWithImage _ tupled _ ))
      }

      override def readAll: Future[IterableOnce[MenuItem]] = {
        val select = for {
          ((t, _), i) <- themes.joinLeft(themeImages).on(_.id === _.idTheme)
            .joinLeft(images.map(dbContext.imageWithoutDataProjection)).on(_._2.map(_.idImage) === _._1)
        } yield (t, i)
        run(select.result).map(e => e.map(themeWithImage _ tupled _ ))
      }

      val d: Factory[Int, List[Int]] = new Factory[Int, List[Int]]() {
        override def fromSpecific(it: IterableOnce[Int]): List[Int] = it.iterator.to(List)

        override def newBuilder: mutable.Builder[Int, List[Int]] = new mutable.ListBuffer[Int]()
      }

      override def delete(m: Int): Future[Boolean] = {
        run(DBIO.sequence(List(dbContext.themeImages.filter(_.idTheme === m).delete,
          dbContext.themes.filter(_.id === m).delete))(d)).map {
          res => res.sum > 0
        }
      }


      def extractRowUpdate(m: MenuItem): ((Int, String, Option[Int], Int, Int, Boolean), Option[Int]) = {
        (
          (m.id.get, m.title, m.themeKey, m.x, m.y, Type(m.`type`)), {
          m.image map (_.id)
        }
        )
      }

      override def update(m: MenuItem): Future[Option[MenuItem]] = {
        val mi = for {
          (t, ti) <- themes.joinLeft(themeImages).on(_.id === _.idTheme)
        } yield (t, ti.map(_.idImage))
        run(mi.update(extractRowUpdate(m))) map {
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

    def update(forPatch: ImgLinkOb): Future[Option[ImgLinkOb]] = {
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


