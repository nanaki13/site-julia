package bon.jo

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import bon.jo.SiteModel.OkResponse
import org.json4s.{CustomSerializer, DefaultFormats}
import org.json4s.JsonAST.{JInt, JNothing, JNull}

import scala.concurrent.{ExecutionContext, Future}




trait JsonIn {

  import org.json4s.native.Serialization.read

  import CustomJs._


}
