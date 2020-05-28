package bon.jo.service

import bon.jo.juliasite.model.Schema
import bon.jo.service.Services.{themeMapping, themeWithImage}
import bon.jo.{RawImpl, WebServiceCrud, js}

import scala.concurrent.Future

trait WebMenuSevice extends Service[RawImpl.ItemRawExport] with WebServiceCrud[RawImpl.ItemRawExport,Int] {
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
    val o1 = run(themes.filter(_.id === m.id).update(menuUpdate(m)))
    val o2 = m.image.asOption.map(
      idImage => run(themeImages.filter(_.idTheme === m.id).map(_.idImage).update(idImage))).getOrElse(Future.successful(0) ).flatMap  {
      e =>
        if(e == 0){
          run(themeImages += (m.id,m.image.v))
        }else{
          Future.successful(e)
        }
    }



    Future.sequence(Seq(o1, o2)).map {
      e => {
        if (e.sum > 0) {
          Some(m)
        } else {
          None
        }
      }
    }

  }

  override def ressourceName: String = "menu"

}
