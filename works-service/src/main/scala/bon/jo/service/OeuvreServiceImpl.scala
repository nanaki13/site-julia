package bon.jo.service

import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.RawImpl.OeuvreRawExport
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import bon.jo.{RawImpl, RootCreator, WebServiceCrud}

import scala.concurrent.{ExecutionContext, Future}



class OeuvreServiceImpl(val dbContext: RepositoryContext with SiteRepository)(implicit val exe: ExecutionContext,val manifest: Manifest[RawImpl.OeuvreRawExport]) extends OeuvreService with WebOeuvreService {
  override def before(implicit m: Materializer): Option[Route] = None


}


