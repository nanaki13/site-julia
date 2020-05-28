package bon.jo.service

import bon.jo.{RawImpl, RootCreator}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.Future

trait MenuService extends IntId[RawImpl.ItemRawExport] with WebMenuSevice with RootCreator[RawImpl.ItemRawExport,Int] {
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


}
