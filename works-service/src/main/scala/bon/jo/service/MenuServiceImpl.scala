package bon.jo.service

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import bon.jo.service.Services.{themeMapping, themeWithImage}
import bon.jo.{RawImpl, RootCreator, WebServiceCrud, js}
import slick.dbio.Effect.Write
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}




class MenuServiceImpl(val dbContext: RepositoryContext with SiteRepository) (implicit val manifest: Manifest[RawImpl.ItemRawExport])
  extends  MenuService {
  override def before(implicit  m: Materializer): Option[Route] = None

}


