package bon.jo

import bon.jo.RawImpl.OeuvreRawExport
import bon.jo.SiteModel.{ImgLink, ImgLinkOb, OkResponse}
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import org.json4s.JsonAST.{JInt, JNothing, JNull, JString}
import org.json4s.{CustomSerializer, DefaultFormats, Formats}
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

  def themeMapping(themeDB: Schema.Themes): RawImpl.ItemRawExport = {
    // (id, name,idThemeParent,x,y, final_theme)
    RawImpl.ItemRawExport(themeDB._1, themeDB._2, "", js.BigInt.Null, js.BigInt(themeDB._3), js.BigInt(themeDB._4), js.BigInt(themeDB._5))
  }

  def themeWithImage(themeDB: Schema.Themes, imgDb: Option[Schema.ImagesWithoutData]): RawImpl.ItemRawExport = {
    themeMapping(themeDB).copy(image = imgDb.map(_._1).map(js.BigInt(_)).getOrElse(js.BigInt.Null))
  }

  def imageMapping(imgDb: Schema.ImagesWithoutData): Int = {
    imgDb._1
  }


  trait MenuService extends Service[RawImpl.ItemRawExport] {

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
      run(insert map {case 1 => Some(t);case 0 => None} )
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
    //
    //    object OeuvreAndPosition {
    //      def apply(o: Schema.Oeuvre, x: Int = 0, y: Int = 0, image: Option[ImgLinkOb] = None, tk: Option[Int] = None): OeuvreAndPosition = OeuvreAndPosition(o.id,
    //        o.title,
    //        o.description,
    //        o.dimensionX,
    //        o.dimensionY,
    //        o.creation,
    //        x,
    //        y,
    //        image,
    //        tk)
    //    }
    //
    //    case class OeuvreAndPosition(id: Int, title: String, description: String, dimensionX: Float, dimensionY: Float,
    //                                 creation: Int, x: Int, y: Int, image: Option[ImgLinkOb], themeKey: Option[Int]) extends OkResponse {
    //      def toOeuvre: Schema.Oeuvre = Schema.Oeuvre(id, title, description, dimensionX, dimensionY, creation)
    //    }


  }

  trait OeuvreService extends Service[RawImpl.OeuvreRawExport] with WebServiceCrud[RawImpl.OeuvreRawExport]

  trait ServiceFactory {
    def dbContext: RepositoryContext with SiteRepository

    val _dbContext: RepositoryContext with SiteRepository = dbContext

    import _dbContext.profile.api._

    trait WebImageSevice extends ImageService with WebServiceCrud[RawImpl.ImageRawExport] {
      override def createEntity(m: RawImpl.ImageRawExport): Future[Option[RawImpl.ImageRawExport]] = saveImage(Some(new Array[Byte](0)), m.id, m.link.substring(m.link.lastIndexOf('.') + 1), m.link.substring(0, m.link.lastIndexOf('.'))).map(e => {
        e map { ee =>
          m.copy(ee._1)
        }
      })


      override def readEntity(m: Int): Future[Option[RawImpl.ImageRawExport]] = {
        val selectAndMap = dbContext.images.filter(_.id === m).map(t => (t.id, t.contentType, t.name)).result.headOption.map(ee =>
          ee map { e =>
            RawImpl.ImageRawExport(e._1, e._1 + "." + e._2.substring(e._2.lastIndexOf('/') + 1))
          }
        )
        dbContext.db.run(selectAndMap)
      }

      override def readAll: Future[IterableOnce[RawImpl.ImageRawExport]] = {
        val selectAndMap = dbContext.images.map(t => (t.id, t.contentType, t.name)).result.map(ee =>
          ee map { e =>
            RawImpl.ImageRawExport(e._1, e._1 + "." + e._2.substring(e._2.lastIndexOf('/') + 1))
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

    trait WebOeuvreService extends OeuvreService with WebServiceCrud[RawImpl.OeuvreRawExport] {
      override val dbContext = _dbContext


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

    trait WebMenuSevice extends MenuService with WebServiceCrud[RawImpl.ItemRawExport] {
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

      override val dbContext: RepositoryContext with SiteRepository = _dbContext
    }

  }

  trait ImageService extends Service[RawImpl.ImageRawExport] {


    import dbContext.profile.api._

    def addImagesMenu(b: Array[Byte], contentType: String, name: String): Future[Option[(Int, String)]] = dbContext.addImagesMenu(b, contentType, name)

    /**
     *
     * @param byttes
     * @param contentType
     * @return image id and contentType
     */
    def saveImage(byttes: Option[Array[Byte]], id: Int, contentType: String, name: String): Future[Option[(Int, String, String)]] = {
      val sbInsert: FixedSqlAction[Int, NoStream, Write] = dbContext.images += (id, contentType, byttes.get, name)

      run(sbInsert) map { case 1 => Some(id, contentType, name) }
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


object T extends App {

  object reader extends bon.jo.JsonIn

  import reader._


  import js._

  case class ItemRawExportP(id: Int,
                            text: String,
                            link: String,
                            image: Option[Int],
                            parent: js.BigInt,
                            x: Int,
                            y: Int)


  object CustomJs extends CustomSerializer[js.BigInt](format =>
    ( {
      case JInt(s) => js.BigInt(s.toInt)
      case JNull => js.BigInt(0, true)
      case JNothing => js.BigInt(0, true)
    }, {

      case e: js.BigInt => JInt(e.v)
    }))

  implicit val formatsIn: Formats = DefaultFormats + LocalDateSerializer + CustomJs

  import T.formatsIn

  val s = """{"id" : 1,"image" : 0, "text" : "daz", "link" : "daz", "children" : [], "oeuvres" : [ 2 ] , "x" : 0 , "y" : 0 }"""
  println(org.json4s.native.Serialization.read[RawImpl.ItemRawExport](s))


  println(org.json4s.native.Serialization.write[RawImpl.ItemRawExport](RawImpl.ItemRawExport(1, "", "", 1, 1, 1, 1)))
}