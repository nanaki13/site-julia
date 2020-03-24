package bon.jo

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import bon.jo.SiteModel.{OkResponse, Operation}

import scala.concurrent.ExecutionContext

trait RootCreator[WebMessage <: OkResponse] extends Directives with JsonParsing with RouteHandle {

  self: WebServiceCrud[WebMessage] =>

  def stadard(
               implicit executionContext: ExecutionContext,
               manifest: Manifest[WebMessage],
               m: Materializer
             ): Route = {

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
        entity(as[WebMessage]) { e => {
          implicit val ok = StatusCodes.Created
          handle(createEntity(e),
            s"error adding  ${ressourceName}",
            s"cant find cretaed  ${ressourceName}", StatusCodes.NotFound)
        }
        }

      },
      patch {
        implicit val okStatus = StatusCodes.NoContent
        entity(as[WebMessage]) { forPatch =>
          handle(self.updateEntity(forPatch),
            s"error when update  ${ressourceName}",
            "update not donz", StatusCodes.NotModified)

        }
      })
  }

  def crudRoot(
                implicit executionContext: ExecutionContext,
                manifest: Manifest[WebMessage],
                m: Materializer
              ): Route = {
    implicit def inJson: FromEntityUnmarshaller[WebMessage] = unMarsh[WebMessage]

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

  def before (implicit executionContext: ExecutionContext,m: Materializer): Option[Route]
}
