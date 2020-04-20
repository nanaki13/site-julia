package bon.jo

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import bon.jo.SiteModel.OkResponse

import scala.concurrent.ExecutionContext

object JsonParsing extends JsonParsing

trait JsonParsing extends  JsonOut {
  // def inJson[R <: OkResponse](implicit manifest: Manifest[R], m: Materializer, ec: ExecutionContext): FromEntityUnmarshaller[R] = unMarsh[R]

  implicit def out[T <: OkResponse](implicit statusCode: StatusCode): ToResponseMarshaller[T] = jsonEntityWithStatus[T](statusCode)

  implicit def outList[T <: Seq[OkResponse]](implicit statusCode: StatusCode): ToResponseMarshaller[T] = jsonEntityWithStatus[T](statusCode)
}
