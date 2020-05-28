package bon.jo.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.RawImpl.TextId
import bon.jo.juliasite.model.Schema
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}
import bon.jo.{RawImpl, RootCreator, WebServiceCrud}

import scala.concurrent.{ExecutionContext, Future}

trait ServicesCont {
  def menuService: MenuService

  def imageService: ImageService

  def oeuvreService: OeuvreService

  def textService: TextService
}

class TextService(val dbContext: RepositoryContext with SiteRepository)(implicit val manifest: Manifest[RawImpl.TextExport], exe: ExecutionContext) extends Service[RawImpl.TextExport] with WebServiceCrud[RawImpl.TextExport,(String,Int)] with RootCreator[RawImpl.TextExport,(String,Int)] {

  import dbContext.profile.api._

  override def createEntity(m: RawImpl.TextExport): Future[Option[RawImpl.TextExport]] = {
    run((texts += Schema.SiteText( m.id.uid,m.id.index, m.text)) map {
      e =>
        if (e == 1) {
          Some(m)
        } else {
          None
        }
    })
  }


  override def readEntity(id:(String,Int)): Future[Option[RawImpl.TextExport]] = {
    run(texts.filter(e=>e.uid === id._1 && e.index === id._2 ).result.headOption).map {
      o =>
        o.map(e => RawImpl.TextExport( TextId(e.uid,e.index), e.text))
    }
  }

  override def readAll: Future[IterableOnce[RawImpl.TextExport]] = run(texts.result).map {
    o =>
      o.map(e => RawImpl.TextExport(TextId(e.uid,e.index), e.text))
  }

  def findByUid(uid : String): Future[Seq[RawImpl.TextExport]] ={
    run(texts.filter(_.uid === uid).result).map {
      o =>
        o.map(e => RawImpl.TextExport( TextId(e.uid,e.index), e.text))
    }
  }
  override def deleteEntity(id: (String,Int)): Future[Boolean] = run(texts.filter(e=>e.uid === id._1 && e.index === id._2 ).delete.map(_ == 1))

  override def updateEntity(text: RawImpl.TextExport): Future[Option[RawImpl.TextExport]] =
    {
      run(texts.filter(e=>e.uid === text.id.uid && e.index === text.id.index).update(Schema.SiteText( text.id.uid,text.id.index, text.text)).map { cnt =>
        if (cnt == 1) {
          Some(text)
        } else {
          None
        }
      })
    }



  override def ressourceName: String = "text"

  override def before(implicit m: Materializer): Option[Route] = {
    Some{
      get{
        parameter(Symbol("uid")){
          uid =>
            handle( findByUid(uid),"Error during process")(StatusCodes.OK)
        }
      }
    }
  }

  override def stringToId(id: List[String]): (String, Int) = {
    (id.head,id.tail.head.toInt)
  }
}
