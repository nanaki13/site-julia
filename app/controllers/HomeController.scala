package controllers

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths

import akka.util.CompactByteString
import controllers.SiteModel.{ImgLink, ImgLinkOb, MenuItem}
import javax.inject._
import play.api.http.HttpEntity
import play.api.libs.Files.TemporaryFile
import play.api.libs.json._
import play.api.mvc._
import controllers.services.Services.{ImageService, MenuService}

import scala.concurrent.{ExecutionContext, Future}

object HomeController {

  case class SubMenuList(parentTheme: Int) {}

  implicit def queryStringBindable(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[SubMenuList] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SubMenuList]] = {
      for {
        from <- intBinder.bind("parentTheme", params)

      } yield {
        from match {
          case Right(from) => Right(SubMenuList(from))
          case _ => Left("Unable to bind an SubMenuList")
        }
      }
    }

    override def unbind(key: String, ageRange: SubMenuList): String = {
      intBinder.unbind("parentTheme", ageRange.parentTheme)
    }
  }
}

object SiteModel {

  case class MenuItem(var id: Option[Int] = None, title: String, parentTheme: Option[Int])

  object MenuItem {
    def apply(tup: (Option[Int], String, Option[Int])): MenuItem = {
      MenuItem(tup._1, tup._2, tup._3)
    }


  }

  case class ImgLinkOb(link: String)

  object ImgLink {

    def apply(id: Int, contentType: String): String =
      s"image/${id}" + (contentType match {
        case "image/jpeg" => ".jpg"
        case "image/png" => ".png"
        case _ => ".jpg"
      })
  }

}

@Singleton
class HomeController @Inject()(cc: ControllerComponents
                               , menuService: MenuService
                               , imageService: ImageService
                              ) extends AbstractController(cc) {


  implicit val residentWrites: OWrites[MenuItem] = Json.writes[MenuItem]
  implicit val readMenuItem: Reads[MenuItem] = Json.reads[MenuItem]
  implicit val readLnk: Reads[ImgLinkOb] = Json.reads[ImgLinkOb]
  implicit val lnkWrites: OWrites[ImgLinkOb] = Json.writes[ImgLinkOb]
  implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global


  def appSummary: Action[AnyContent] = Action {
    Ok(Json.obj("content" -> "Scala Play Angular Seed"))
  }

  def postTest: Action[AnyContent] = Action {
    Ok(Json.obj("content" -> "Post Request Test => Data Sending Success"))
  }


  def read(picture: MultipartFormData.FilePart[TemporaryFile]): (Option[Array[Byte]], String) = {
    // only get the last part of the filename
    // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
    val filename = Paths.get(picture.filename).getFileName
    val fileSize = picture.fileSize
    val contentType = picture.contentType.get


    val bt = new BufferedInputStream(new FileInputStream(picture.ref))
    var byttes: Option[Array[Byte]] = None
    try {
      byttes = Some(bt.readAllBytes())
    } finally {
      bt.close()
    }
    (byttes, contentType)
  }


  def postImages(request: Request[MultipartFormData[TemporaryFile]]): Future[Result] = {
    request.body
      .file("file")
      .map { picture =>
        val (byttes: Option[Array[Byte]], contentType: String) = read(picture)
        imageService.saveImage(byttes, contentType) map {
          case Some((id, ct)) => Ok(Json.obj("link" -> ImgLink(id, ct)))
          case _ => NotFound(Json.obj("content" -> "Don't found the previous immage"))
        }
      }
      .getOrElse {
        Future.successful(NotFound(Json.obj("content" -> "File not found in request")))
      }
  }

  def postImagesAction: Action[MultipartFormData[TemporaryFile]] = Action(parse.multipartFormData) { request =>
    postImages(request) match {
      case r: Result => r
      case _ => NotFound(Json.obj("content" -> "Don't found the previous immage"))
    }
  }

  def postImagesMenuAction = Action.async(parse.multipartFormData) { request =>
    request.body.file("file").map { f => {
      val (byttes, contentType) = read(f)
      val b = byttes.getOrElse(NotFound(Json.obj("content" -> "File not found in request")))
      b match {
        case e: Array[Byte] => imageService.addImagesMenu(e, contentType).map {
          case Some((id, ct)) => Ok(Json.obj("link" -> ImgLink(id, ct)))
          case _ => NotFound(Json.obj("content" -> "Don't found the previous immage"))

        }
      }
    }

    }.getOrElse {
      Future.successful(NotFound(Json.obj("content" -> "File not found in request")))
    }

  }


  def getMenu: Action[AnyContent] = Action.async {

    menuService.getMenu.map(z => Ok(Json.toJson(z)))

  }

  def getSubMenu(sbList: HomeController.SubMenuList) = Action.async {

    val parentId: Int = sbList.parentTheme
    menuService.getSubMenu(parentId) map { l =>
      Ok(Json.toJson(l))
    }

  }

  def addSubMenu(): Action[AnyContent] = Action.async {
    request: Request[AnyContent] =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson
      Json.fromJson[MenuItem](jsonBody.get) match {
        case JsSuccess(r: MenuItem, path: JsPath) => {
          addChildTheme(r);

        }
        case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
      }

  }

  def addChildTheme(t: MenuItem): Future[Result] = {
    menuService.addChildTheme(t) map {
      case Some(v) => Ok(Json.toJson(v))
      case None => NotFound(Json.toJson(t))
    }
  }

  def addMenu(): Action[AnyContent] = Action.async {
    request: Request[AnyContent] =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson
      Json.fromJson[MenuItem](jsonBody.get) match {
        case JsSuccess(r: MenuItem, path: JsPath) => {
          addRootTheme(r);

        }
        case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
      }

  }

  def getImageMenuLink: Action[AnyContent] = Action.async {
    imageService.imageMenuLink().map(e => Ok(Json.toJson(e)))
  }

  def getImage(id: String): Action[AnyContent] = Action.async {
    imageService.getImage(id) map {
      case Some((bites, ctype)) => new Result(ResponseHeader(200), HttpEntity.Strict(CompactByteString(bites), Some(ctype)))
      case None => NotFound
    }
  }


  def addRootTheme(t: MenuItem): Future[Result] = {
    imageService.addRootTheme(t) map {
      case Some(v) => Ok(Json.toJson(v))
      case None => NotFound(Json.toJson(t))
    }
  }

  def okJson(mes: String) = Ok(Json.obj("content" -> mes))
}









