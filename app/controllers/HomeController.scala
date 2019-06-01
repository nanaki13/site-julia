package controllers

import java.io.{BufferedInputStream, FileInputStream}
import java.nio.file.Paths

import akka.util.CompactByteString
import bon.jo.helloworld.juliasite.model._
import bon.jo.helloworld.juliasite.pers._
import javax.inject._
import play.api.http.HttpEntity
import play.api.libs.Files.TemporaryFile
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

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

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  implicit val residentWrites: OWrites[MenuItem] = Json.writes[MenuItem]
  implicit val readMenuItem: Reads[MenuItem] = Json.reads[MenuItem]
  implicit val readLnk: Reads[ImgLinkOb] = Json.reads[ImgLinkOb]
  implicit val lnkWrites: OWrites[ImgLinkOb] = Json.writes[ImgLinkOb]
  implicit val ctx: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val dbConntext = new ApplicationPostgresProfile
  val _import = dbConntext.profile.api

  import _import._


  def appSummary: Action[AnyContent] = Action {
    Ok(Json.obj("content" -> "Scala Play Angular Seed"))
  }

  def postTest: Action[AnyContent] = Action {
    Ok(Json.obj("content" -> "Post Request Test => Data Sending Success"))
  }

  case class  ImgLinkOb(  link : String){

  }
  object ImgLink {

    def apply(id: Int, contentType: String): String = s"image/${id}" + (contentType match {
      case "image/jpeg" => ".jpg"
      case "image/png" => ".png"
      case _ => ".jpg"
    })
  }

  def read(picture: MultipartFormData.FilePart[TemporaryFile]) = {
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

  def postImages(request: Request[MultipartFormData[TemporaryFile]]) = {
    request.body
      .file("file")
      .map { picture =>
        val (byttes, contentType) = read(picture)
        val dbOp = dbConntext.db.run((dbConntext.images += Images(0, contentType, byttes.get)) flatMap {
          _ => {
            dbConntext.images.map(e => (e.id, e.contentType)).sortBy(_._1.desc).result.headOption
          } map {
            case Some((id,ct)) => Ok(Json.obj("link" -> ImgLink(id, ct)))
            case _ => NotFound(Json.obj("content" -> "Don't found the previous immage"))
          }
        })
        Await.result(dbOp, Duration.Inf)
      }
      .getOrElse {
        NotFound(Json.obj("content" -> "File not found in request"))
      }
  }

  def postImagesAction: Action[MultipartFormData[TemporaryFile]] = Action(parse.multipartFormData) { request =>
    postImages(request) match {
      case r: Result => r
      case _ => NotFound(Json.obj("content" -> "Don't found the previous immage"))
    }
  }

  def postImagesMenuAction = Action(parse.multipartFormData) { request =>
    request.body.file("file").map(f => {
      val (byttes, contentType) = read(f)
      val id = byttes match {
        case Some(b) => dbConntext.addImagesMenu(b, contentType)
        case _ => None
      }
      id match {
        case Some((id,ct)) => Ok(Json.obj("content" -> "Element added", "link" -> ImgLink(id, ct)))
        case None => NoContent
      }
    }).getOrElse(NotFound(Json.obj("content" -> "File not found in request")))


  }


  def getMenu: Action[AnyContent] = Action {
    var l: Seq[MenuItem] = Nil
    Await.result(dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent.isEmpty).result) map {
      e => {
        l = e.map(i => {
          MenuItem(Option.apply(i._1), i._2, None)
        })
      }
    }
      , Duration.Inf)
    Ok(Json.toJson(l))

  }

  def getSubMenu(sbList: HomeController.SubMenuList) = Action {

    val parentId: Int = sbList.parentTheme
    val menuItemSeq = Await.result(dbConntext.db.run(dbConntext.themes.filter(_.idThemeParent === parentId).result), Duration.Inf) map {
      i => MenuItem(Option.apply(i._1), i._2, Some(parentId))
    }
    Ok(Json.toJson(menuItemSeq))
  }

  def addSubMenu(): Action[AnyContent] = Action {
    request: Request[AnyContent] =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson
      Json.fromJson[MenuItem](jsonBody.get) match {
        case JsSuccess(r: MenuItem, path: JsPath) => {
          addChildTheme(r);

        }
        case e: JsError => BadRequest(JsError.toJson(e))
      }

  }

  def addChildTheme(t: MenuItem) = {
    val insert = dbConntext.themes += (0, t.title, t.parentTheme)
    val f = dbConntext.db.run(insert) flatMap (_ => {
      dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map(e => {
        e match {
          case Some(tuple) => Some(MenuItem.applyFromDv(tuple))
          case _ => None
        }
      }))
    })
    Await.result(f, Duration.Inf) match {
      case Some(v) => Ok(Json.toJson(v))
      case None => NotFound(Json.toJson(t))
    }
  }

  def addMenu(): Action[AnyContent] = Action {
    request: Request[AnyContent] =>
      val body: AnyContent = request.body
      val jsonBody: Option[JsValue] = body.asJson
      Json.fromJson[MenuItem](jsonBody.get) match {
        case JsSuccess(r: MenuItem, path: JsPath) => {
          addRootTheme(r);

        }
        case e: JsError => BadRequest(JsError.toJson(e))
      }

  }
  def getImageMenuLink = Action {
    val lk =   dbConntext.getImagesMenuLnk().map(e => ImgLinkOb( ImgLink(e._1, e._2))).toList

      Ok(Json.toJson(lk))
  }
  def getImage(id: String): Action[AnyContent] = Action {

    val nf = Some(NotFound);
    var res: Option[Result] = Some(NotFound);
    var resss: Option[Array[Byte]] = None;
    val f = dbConntext.db.run(dbConntext.images.filter(_.id === Integer.parseInt(id)).map(_.imgData).result.headOption) map { ress =>
      ress match {
        case Some(x) => resss = Some(x)
        case None => resss = None
      }

      resss match {
        case Some(bites) => res = Some(new Result(ResponseHeader(200), HttpEntity.Strict(CompactByteString(bites), Some("image/jpeg"))))
        case None => res = nf
      }
    }


    Await.result(f, Duration.Inf)
    res.get

  }


  def addRootTheme(t: MenuItem) = {
    val insert = dbConntext.themes += (0, t.title, None)
    val f = dbConntext.db.run(insert) flatMap (_ => {
      dbConntext.db.run(dbConntext.themes.sortBy(_.id.desc).result.headOption.map(e => {
        e match {
          case Some(tuple) => Some(MenuItem.applyFromDv(tuple))
          case _ => None
        }
      }))
    })
    Await.result(f, Duration.Inf) match {
      case Some(v) => Ok(Json.toJson(v))
      case None => NotFound(Json.toJson(t))
    }
  }

  def okJson(mes: String) = Ok(Json.obj("content" -> mes))
}


case class MenuItem(var id: Option[Int] = None, val title: String, val parentTheme: Option[Int]) {

}

object MenuItem {


  def apply(tup: (Option[Int], String, Option[Int])): MenuItem = {
    MenuItem(tup._1, tup._2, tup._3)
  }

  def applyFromDv(tup: (Int, String, Option[Int])): MenuItem = {
    MenuItem(Option.apply(tup._1), tup._2, tup._3)
  }


}

object Initilaizer extends App {
  val dbConntext = new ApplicationPostgresProfile

  def createDropCreate = {
    dbConntext.createMissing()
    println("coucou")
    dbConntext.dropAll()
   dbConntext.createMissing()
  }
  createDropCreate

}
