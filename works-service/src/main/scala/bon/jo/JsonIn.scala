package bon.jo

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST.{JInt, JNothing, JNull}

import scala.concurrent.{ExecutionContext, Future}




trait JsonIn {

  import org.json4s.native.Serialization.read

  import CustomJs._

  def unMarsh[Ok](implicit manifest: Manifest[Ok], ex: ExecutionContext, m: Materializer): FromEntityUnmarshaller[Ok] = {
    def toJson(s: HttpEntity)(implicit ex: ExecutionContext, m: Materializer): Future[Ok] = {
      s.dataBytes.runReduce(_ ++ _).map(e => {
        try{
          read[Ok](e.utf8String)
        }catch {
          case e : Exception => println(e);e.printStackTrace();throw e
        }

      })
    }

    Unmarshaller.withMaterializer(_ => _ => toJson)
  }
}
