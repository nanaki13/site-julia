package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import bon.jo.helloworld.juliasite.pers._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import bon.jo.helloworld.juliasite.model._
import slick.jdbc.{H2Profile, JdbcProfile}
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc)   {

  implicit val residentWrites = Json.writes[MenuItem]
  implicit val readMenuItem = Json.reads[MenuItem]
 implicit val ctx = scala.concurrent.ExecutionContext.Implicits.global
  val dbConntext = ApplicationInMemmoryH2
  val _import = dbConntext.profile.api

  import _import._
  def allShema = dbConntext.allTableAsSeq.map(_.schema).reduce((a,b)=> a ++ b)
 //  val create = dbConntext.asInstanceOf[Repository].allShema.create
  Await.result(dbConntext.db.run( allShema.create) , Duration.Inf)

  
   var menu = List(MenuItem(1,"toto"))
  def appSummary = Action {
    Ok(Json.obj("content" -> "Scala Play Angular Seed"))
  }

  def postTest = Action {
    Ok(Json.obj("content" -> "Post Request Test => Data Sending Success"))
  }

  def getMenu = Action {
    var l : Seq[MenuItem] = Nil 
    Await.result(dbConntext.db.run(dbConntext.themes.result) map {
       e => {  l = e.map(i => {MenuItem(i._1,i._2)}) }
    }
     , Duration.Inf)
    Ok(Json.toJson(l))

  }

   def addMenu() = Action {
    request: Request[AnyContent] =>
    val body: AnyContent          = request.body
    val jsonBody: Option[JsValue] = body.asJson
    Json.fromJson[MenuItem](jsonBody.get) match {
      case JsSuccess(r: MenuItem, path: JsPath) =>  { addTheme(r); Ok(Json.obj("content" -> "menu added"))}
      case e: JsError => BadRequest(JsError.toJson(e))}

    }
   
 

  def addTheme(t : MenuItem) = {
    val  insert = dbConntext.themes +=(t.id,t.title)
    val f = dbConntext.db.run(insert)
    Await.result(f, Duration.Inf)
  }
 }

case class MenuItem(val id : Int, val title : String)
