package bon.jo

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, RequestContext, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import bon.jo.SiteModel._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Routes extends Directives with JsonOut with JsonIn {


  /*
    menuUrl : "/api/menu",
  subMenuUrl : "/api/submenu",
  oeuvreUrl : "/api/oeuvre",
  imageUrl : "/api/image",
   */


  implicit def out[T <: OkResponse](implicit statusCode: StatusCode): ToResponseMarshaller[T] = jsonEntity[T]

  implicit def outList[T <: Seq[OkResponse]](implicit statusCode: StatusCode): ToResponseMarshaller[T] = jsonEntity[T]

  implicit def outError: ToResponseMarshaller[MyFailure] = jsonEntityWithStatus(StatusCodes.InternalServerError)

  implicit def inJson[R <: OkResponse](implicit manifest: Manifest[R], m: Materializer, ec: ExecutionContext): FromEntityUnmarshaller[R] = unMarsh[R]

  def toJson[Other](implicit statusCode: StatusCode): ToResponseMarshaller[Other] = jsonEntityWithStatus(statusCode)

  def cJson[Other](e: Other)(implicit statusCode: StatusCode) = complete({
    implicit val out = toJson[Other]
    e
  })

  def posteMenu(implicit m: Materializer,
                ec: ExecutionContext, ms: Services.MenuService): Route = post {
    entity(as[MenuItem]) {
      mi => {
        implicit val ok = StatusCodes.Created
        handle(ms.addMenu(mi),
          "error adding menu",
          "cant find cretaed menu", StatusCodes.NotFound)
      }
    }

  }


  def doWithContext(ctx: RequestContext): Route = {
    implicit val m: Materializer = ctx.materializer
    implicit val ec: ExecutionContext = ctx.executionContext

    implicit val ms: Services.MenuService = Services.menuService
    implicit val imageService: Services.ImageService = Services.imageService
    val log = ctx.log



    //implicit val actor : Actor = ctx.actor
    concat(path("menu") {
      concat(get {
        implicit val st = StatusCodes.OK
        handle(ms.getMenu, "error when getting menu")
      }
        ,
        post {
          posteMenu
        }
      )
    }
      ,
      path("submenu") {
        concat(get {
          parameters('theme_key) { tk =>
            implicit val st = StatusCodes.OK
            handle(ms.getSubMenu(tk.toInt), "error when getting submenu")
          }
        },
          post {
            posteMenu
          })
      }
      ,
      path("oeuvre") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "oeuvre"))
        }
      },
      path("image" / Segment) { filenameOrId =>
        concat(
          get {

            onComplete(
              for (fStart <- imageService.getImage(filenameOrId.substring(0, filenameOrId.lastIndexOf('.')))
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
          },

          delete {
            implicit val st = StatusCodes.Accepted
            handle(imageService.deleteImage(filenameOrId.toInt).map(Operation.apply), "error when delete image")
          }
        )

      },
      path("image") {
        concat(get {
          implicit val okStatus = StatusCodes.OK
          handle(imageService.imagesLink(), "error when getting image")
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
                  imageService.saveImage(Some(data), ct, name).map(e => {
                    Some(ImgLinkOb(e.get._1, e.get._2, ImgLink(e.get._1, e.get._2), name))
                  })
                })
                implicit val okStatus = StatusCodes.Created
                handle(processParsed,
                  "error when create image",
                )
            }
          },
          patch {
            implicit val okStatus = StatusCodes.NoContent
            entity(as[ImgLinkOb]) { forPatch =>
              handle(imageService.update(forPatch),
                "error when update",
                "update not donz", StatusCodes.NotModified)

            }
          }

        )
      }
    )
  }

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


  def allRoutes(implicit ec: ExecutionContext): Route = {


    pathPrefix("api") {
      extractRequestContext { ctx => {
        doWithContext(ctx)
      }
      }
    }
  }
}
