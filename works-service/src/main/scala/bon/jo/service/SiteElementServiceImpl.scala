package bon.jo.service

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.RawImpl
import bon.jo.juliasite.model.Schema.{Descri, SiteElement}
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}

import scala.concurrent.{ExecutionContext, Future}

class SiteElementServiceImpl(val dbContext: RepositoryContext with SiteRepository)(implicit val exe: ExecutionContext,val manifest: Manifest[RawImpl.SiteElementExport]) extends SiteElementService with WebSiteElementService {
  import dbContext.profile.api._


  override def before(implicit m: Materializer): Option[Route] = None



}
