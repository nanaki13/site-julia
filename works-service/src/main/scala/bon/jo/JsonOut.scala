package bon.jo

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCode}
import org.json4s.DefaultFormats

trait JsonOut {

  import org.json4s.native.Serialization.write

  import CustomJs._

  def jsonEntity[Ok]: ToResponseMarshaller[Ok] =
    Marshaller.withFixedContentType(MediaTypes.`application/json`) { item =>
      val data = write(item)
      HttpResponse(entity = HttpEntity(data))
    }

  def jsonEntityWithStatus[Ok](statusCode: StatusCode): ToResponseMarshaller[Ok] =
    Marshaller.withFixedContentType(MediaTypes.`application/json`) { item =>
      val data = write(item)
      HttpResponse(entity = HttpEntity(data), status = statusCode)
    }
}
