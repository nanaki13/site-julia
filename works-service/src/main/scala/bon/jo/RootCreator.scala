package bon.jo

import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import bon.jo.SiteModel.{OkResponse, Operation}
import org.json4s.native.Serialization.read

import scala.concurrent.{ExecutionContext, Future}
import CustomJs._
import akka.http.scaladsl.model.Uri.Path
trait RootCreator[WebMessage <: OkResponse,ID] extends Directives with JsonParsing with RouteHandle {

  self: WebServiceCrud[WebMessage,ID] =>
  implicit val manifest: Manifest[WebMessage]

  def stringToId(id:  List[String]) : ID


  def stadard(
               implicit executionContext: ExecutionContext,
               m: Materializer
             ): Route = {

    def unMarsh[All](implicit manifest: Manifest[All],  m: Materializer): FromEntityUnmarshaller[All] = {
      def toJson(s: HttpEntity): Future[All] = {
        s.dataBytes.runReduce(_ ++ _).map(e => {
          try{
            read[All](e.utf8String)
          }catch {
            case e : Exception => println(e);e.printStackTrace();throw e
          }

        })
      }

      Unmarshaller.withMaterializer(_ => _ => toJson)
    }
    concat(pathPrefix(RemainingPath) { id: Path =>
      concat(get {

        implicit val st = StatusCodes.OK
        handle(self.readEntity(stringToId(readPath(id))), s"error when getting ${ressourceName}")
      },
        delete {
          implicit val st = StatusCodes.NoContent

          handle(self.deleteEntity(stringToId(readPath(id))).map(Operation.apply), s"error when delete  ${ressourceName}")
        })
    },
      get {
        implicit val st = StatusCodes.OK
        handle(self.readAll, s"error when getting  ${ressourceName}")
      },
      post {

        implicit val j = unMarsh[WebMessage]
        entity(as[WebMessage]) { e => {
          implicit val ok = StatusCodes.Created
          handle(createEntity(e),
            s"error adding  ${ressourceName}",
            s"cant find cretaed  ${ressourceName}", StatusCodes.NotFound)
        }
        }

      },
      patch {
        implicit val okStatus: StatusCodes.Success = StatusCodes.NoContent
        implicit val j = unMarsh[WebMessage]
        entity(as[WebMessage]) { forPatch =>
          handle(self.updateEntity(forPatch),
            s"error when update  ${ressourceName}",
            "update not donz", StatusCodes.NotModified)

        }
      })
  }

  def readPath(p : Path): List[String] = {
    var cr = p
    var ps : List[String] =   Nil
    for(_ <- 0 until p.length) {
      if(!cr.startsWithSlash){
        ps = ps :+ cr.head.toString
      }
      cr = cr.tail
    }
    ps
  }
  def crudRoot(
                implicit executionContext: ExecutionContext,
                m: Materializer
              ): Route = {


    before match {
      case Some(v) => pathPrefix(ressourceName) {
        concat(
          v, stadard)
      }
      case _ => pathPrefix(ressourceName) {
        stadard
      }
    }
  }

  def before(implicit m: Materializer): Option[Route]
}
