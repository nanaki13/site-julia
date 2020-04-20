package bon.jo

import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import bon.jo.SiteModel.{OkResponse, Operation}
import org.json4s.native.Serialization.read

import scala.concurrent.{ExecutionContext, Future}
import CustomJs._
trait RootCreator[WebMessage <: OkResponse] extends Directives with JsonParsing with RouteHandle {

  self: WebServiceCrud[WebMessage] =>
  implicit val manifest: Manifest[WebMessage]



  def stadard(
               implicit executionContext: ExecutionContext,
               m: Materializer
             ): Route = {

    def unMarsh[WebMessage](implicit manifest: Manifest[WebMessage],  m: Materializer): FromEntityUnmarshaller[WebMessage] = {
      def toJson(s: HttpEntity): Future[WebMessage] = {
        s.dataBytes.runReduce(_ ++ _).map(e => {
          try{
            println(e.utf8String)
            read[WebMessage](e.utf8String)
          }catch {
            case e : Exception => println(e);e.printStackTrace();throw e
          }

        })
      }

      Unmarshaller.withMaterializer(_ => _ => toJson)
    }
    concat(pathSuffix(IntNumber) { id: Int =>
      concat(get {
        implicit val st = StatusCodes.OK
        handle(self.readEntity(id), s"error when getting ${ressourceName}")
      },
        delete {
          implicit val st = StatusCodes.NoContent

          handle(self.deleteEntity(id).map(Operation.apply), s"error when delete  ${ressourceName}")
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

  def before(implicit executionContext: ExecutionContext, m: Materializer): Option[Route]
}
