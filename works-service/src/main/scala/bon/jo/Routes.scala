package bon.jo

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, MediaType, MediaTypes, Multipart, StatusCode, StatusCodes}
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.http.scaladsl.server.{Directives, RequestContext, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import akka.util.ByteString
import bon.jo.Services.ImageService
import bon.jo.SiteModel.{ImgLinkOb, MenuItem, OkResponse}
import akka.event.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Routes extends Directives with JsonOut with JsonIn {


  /*
    menuUrl : "/api/menu",
  subMenuUrl : "/api/submenu",
  oeuvreUrl : "/api/oeuvre",
  imageUrl : "/api/image",
   */

  implicit def out[T <: OkResponse]: ToResponseMarshaller[T] = jsonEntity[T]

  implicit def outList[T <: Seq[OkResponse]]: ToResponseMarshaller[T] = jsonEntity[T]

  implicit def outError: ToResponseMarshaller[MyFailure] = jsonEntityWithStatus(StatusCodes.InternalServerError)

  implicit def inJson[Ok](implicit manifest: Manifest[Ok], m: Materializer, ec: ExecutionContext): FromEntityUnmarshaller[Ok] = unMarsh[Ok]

  def toJson[Other](implicit statusCode: StatusCode): ToResponseMarshaller[Other] = jsonEntityWithStatus(statusCode)

  def cJson[Other](e: Other)(implicit statusCode: StatusCode) = complete({
    implicit val out = toJson[Other]
    e
  })

  def posteMenu(implicit m: Materializer,
                ec: ExecutionContext, ms: Services.MenuService): Route = post {
    entity(as[MenuItem]) {
      mi => {
        onComplete(ms.addMenu(mi)) {
          case Success(v) if v.isDefined => complete(v.get)
          case Failure(exception) => complete(MyFailure(s"error : $exception"))
          case _ => cJson(MyFailure("can't find new menu"))(StatusCodes.NotModified)
        }
      }
    }

  }

  def saveImage(e: ByteString)(implicit imageService: ImageService): ImgLinkOb = {
    // imageService.saveImage(Some(e.toArray))
    null
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
        onComplete(ms.getMenu) {
          case Success(value) => complete(value)
          case Failure(exception) => complete(MyFailure(s"error : $exception"))
        }
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
          parameters('theme_key) { tk => {
            onComplete(ms.getSubMenu(tk.toInt)) {
              case Success(v) => complete(v)
              case Failure(exception) => complete(MyFailure(s"error : $exception"))
              case _ => cJson(MyFailure("can't find sub menu"))(StatusCodes.NotModified)
            }
          }
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
      }, path("image" / Segment) { filename =>
        println(filename)
        onComplete(
          for (fStart <- imageService.getImage(filename.substring(0, filename.lastIndexOf('.')))
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
      path("image") {
        concat(get {
          onComplete(imageService.imageMenuLink()) {
            case Success(value) => complete(value)
            case Failure(exception) => complete(MyFailure(s"error : $exception"))
          }
        },
          post {
            fileUpload("file") {
              bs =>
                println(bs._1.fileName)
                onComplete {
                  bs._2.runReduce(_ ++ _).map(agg =>
                    imageService.saveImage(Some(agg.toArray), bs._1.contentType.toString()).map(e => {
                      ImgLinkOb(s"/api/image/${e.get._1}.${e.get._2.split("/")(1)}")
                    })
                  )
                } {
                  case Success(value) => complete(value)
                  case Failure(exception) => complete(MyFailure(s"error when reading file: $exception"))
                }

            }
          })
      }
    )
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
