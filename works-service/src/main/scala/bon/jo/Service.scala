package bon.jo

import bon.jo.SiteModel.OkResponse
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.TableQuery

import scala.concurrent.Future

trait Service[WebMessage <: OkResponse] {


  val dbContext: RepositoryContext with SiteRepository

  def oeuvres = dbContext.oeuvres

  val db = dbContext.db
  def images: TableQuery[dbContext.ImagesTable] = dbContext.images


  def themes = dbContext.themes

  def oeuvreImages = dbContext.oeuvreImages

  def themesOeuvres = dbContext.themesOeuvres

  def themeImages = dbContext.themeImages

  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = dbContext.run(a)

}
