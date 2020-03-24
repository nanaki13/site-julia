package bon.jo

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import bon.jo.juliasite.pers.{RepositoryContext, SiteRepository}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class ImageServiceImpl(override val dbContext: RepositoryContext with SiteRepository) extends ImageService with WebImageSevice with RouteHandle {

  override def before(implicit executionContext: ExecutionContext, m: Materializer): Option[Route] = Some {
    concat(get {
      pathSuffix(Segment) { filenameOrId =>
        onComplete(
          for (fStart <- getImage(filenameOrId.substring(0, filenameOrId.lastIndexOf('.')))
               ) yield {
            fStart.map(e => {
              val md: MediaType.Binary = MediaType.parse(e._2).getOrElse(MediaTypes.`application/octet-stream`).asInstanceOf[MediaType.Binary]
              val ct: ContentType = ContentType(md)
              HttpEntity.apply(ct, e._1)
            })
          }) {
          case Success(v) => complete(v)
          case Failure(exception) => complete(MyFailure(s"error : $exception"))
        }
      }
    },
      post {
        entity(as[Multipart.FormData]) {
          formData =>
            val formMap: Future[Map[String, Any]] = formData.parts.mapAsync[(String, Any)](1) {

              case b: Multipart.BodyPart if b.name == "file" =>

                b.entity.dataBytes.runReduce(_ ++ _).map(bs => b.name -> (bs.toArray, b.entity.contentType.toString()))
              case b: Multipart.BodyPart =>
                // collect form field values

                b.toStrict(2.seconds).map(strict =>
                  b.name -> strict.entity.data.utf8String)
            }.runFold(Map.empty[String, Any])((map, tuple) => map + tuple)

            val processParsed = formMap.flatMap(parsedMap => {
              val (data, ct) = parsedMap("file").asInstanceOf[(Array[Byte], String)]
              val name = parsedMap("image_name").toString
              val id = parsedMap("id").toString
              saveImage(Some(data), id.toInt, ct, name, "").map(e => {
                e.map { t => RawImpl.ImageRawExport(t._1, t._3 + "." + t._2, t._4) }
              })
            })
            implicit val okStatus = StatusCodes.Created
            handle(processParsed,
              "error when create image",
            )
        }

      })
  }


}



