package bon.jo

import java.io.File

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, RequestContext, Route}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.Materializer
import bon.jo.Services.{ServiceFactory, WebServiceCrud}
import bon.jo.SiteModel._
import bon.jo.juliasite.pers.{PostgresRepo, RepositoryContext, SiteRepository}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Routes extends Directives with JsonOut with JsonIn {


  object ServiceFactoryImpl extends ServiceFactory {
    override def dbContext: RepositoryContext with SiteRepository = PostgresRepo
  }

  trait RootCreator[WebMessage <: OkResponse] {

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
            implicit val st = StatusCodes.Accepted

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

    def before: Option[Route]
  }

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


  def doWithContext(ctx: RequestContext): Route = {
    implicit val m: Materializer = ctx.materializer
    val ec: ExecutionContext = ctx.executionContext

    implicit object menuService extends ServiceFactoryImpl.WebMenuSevice with RootCreator[RawImpl.ItemRawExport] {
      override implicit val ctx: ExecutionContext = ec

      override def before: Option[Route] = None

      //        Some {
      //        get {
      //          implicit val st = StatusCodes.OK
      //          parameters(Symbol("theme_key").?) {
      //            case Some(v) => handle(getSubMenu(v.toInt), "error when getting oeuvre of threme")
      //            case _ => handle(getMenu, "error when getting all oeuvres")
      //          }
      //        }
      //      }
    }
    implicit object imageService extends ServiceFactoryImpl.WebImageSevice with RootCreator[RawImpl.ImageRawExport] {
      override implicit val ctx: ExecutionContext = ec

      override def before: Option[Route] = Some {
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
                  saveImage(Some(data), id.toInt, ct, name).map(e => {
                    Some(ImgLinkOb(e.get._1, e.get._2, ImgLink(e.get._1, e.get._2), name))
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
    implicit object oeuvreService extends ServiceFactoryImpl.WebOeuvreService with RootCreator[RawImpl.OeuvreRawExport] {
      override implicit val ctx: ExecutionContext = ec

      override def before: Option[Route] = None
    }

    {
      implicit val eccc: ExecutionContext = ec


      concat(imageService.crudRoot, menuService.crudRoot, oeuvreService.crudRoot)

    }

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
    println((new File(".")).getAbsolutePath)
    val static =
      pathPrefix("julia") {
        getFromDirectory("html")
      }
    concat(static, pathPrefix("api") {
      extractRequestContext { ctx => {
        doWithContext(ctx)
      }
      }
    })

  }
}
