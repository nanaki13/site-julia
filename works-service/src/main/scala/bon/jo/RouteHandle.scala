package bon.jo

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import bon.jo.SiteModel.OkResponse

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RouteHandle extends Directives {

  import JsonParsing._

  def handle(process: Future[IterableOnce[OkResponse]], errorMessage: String, noneMessage: String = "not found", noneStatus: StatusCode = StatusCodes.NotFound)(implicit okStatus: StatusCode): Route = {
    onComplete(process) {
      case Success(value) => value match {
        case Some(v) => complete(v)
        case l: Seq[OkResponse] => complete(l)
        case None => cJson(MyFailure(s"$noneMessage"))(noneStatus)
      }
      case Failure(exception) => cJson(MyFailure(s"$errorMessage : $exception"))(StatusCodes.InternalServerError)
    }

  }

  def handle(process: Future[OkResponse], errorMessage: String)(implicit okStatus: StatusCode): Route = {
    onComplete(process) {
      case Success(value) => complete(value)
      case Failure(exception) => cJson(MyFailure(s"$errorMessage : $exception"))(StatusCodes.InternalServerError)
    }

  }

  implicit def outError: ToResponseMarshaller[MyFailure] = jsonEntityWithStatus(StatusCodes.InternalServerError)


  def toJson[Other](implicit statusCode: StatusCode): ToResponseMarshaller[Other] = jsonEntityWithStatus(statusCode)

  def cJson[Other](e: Other)(implicit statusCode: StatusCode) = complete({
    implicit val out = toJson[Other]
    e
  })

}
