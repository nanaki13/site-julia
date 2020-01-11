//package controllers
//
//import bon.jo.juliasite.pers.H2Repo
//import controllers.SiteModel.MenuItem
//import controllers.services.Services.{ImageService, MenuService, OeuvreService, SericeImpl, Service}
//import org.scalatestplus.play._
//import org.scalatestplus.play.guice._
//import play.api.libs.json.{Json, Reads}
//import play.api.test._
//import play.api.test.Helpers._
//
//import scala.concurrent.Await
//import scala.concurrent.duration.Duration
//
//
///**
//  * Add your spec here.
//  * You can mock out a whole application including requests, plugins etc.
//  *
//  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
//  */
//class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
//
//
//  trait serviceAll extends Service with ImageService with MenuService with OeuvreService {
//    override val dbc = dbConntext
//  }
//
//  object serviceImpl extends SericeImpl(H2Repo) with serviceAll
//
//  implicit val ec = serviceImpl.ctx
//  Await.result(serviceImpl.dbc.createMissing().map {
//    _ => {
//      "service can createz" should {
//
//        "create a root theme" in {
//          val controller = new HomeController(stubControllerComponents(), serviceImpl, serviceImpl, serviceImpl)
//          val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))
//
//          status(home) mustBe OK
//          contentType(home) mustBe Some("application/json")
//          val resultJson = contentAsJson(home)
//          resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//          val item = MenuItem(None, "test", None)
//          val ad = controller.addRootTheme(MenuItem(None, "test", None))
//          status(ad) mustBe OK
//          contentType(ad) mustBe Some("application/json")
//          implicit val residentFormat = Json.format[MenuItem]
//          (Json.toJson(contentAsJson(ad)) \ "title" mustBe)  (Json.toJson(item) \ "title")
//          val menu =  controller.getMenu.apply(FakeRequest(GET, "/"))
//          status(menu) mustBe OK
//          implicit val reader: Reads[ MenuItem] = Json.reads[ MenuItem]
//          val l : List[MenuItem]  = contentAsJson(menu).as[List[MenuItem]]
//          l mustBe List(MenuItem(Some(1),"test",None))
//        }
//      }
//    }
//
//  }, Duration.Inf)
//
//
//  //  "HomeController GET" should {
//  //
//  //    "render the appSummary resource from a new instance of controller" in {
//  //
//  //      val controller = new HomeController(stubControllerComponents(),serviceImpl,serviceImpl)
//  //      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))
//  //
//  //      status(home) mustBe OK
//  //      contentType(home) mustBe Some("application/json")
//  //      val resultJson = contentAsJson(home)
//  //      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//  //    }
//  //
//  //    "render the appSummary resource from the application" in {
//  //      val controller = inject[HomeController]
//  //      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))
//  //
//  //      status(home) mustBe OK
//  //      contentType(home) mustBe Some("application/json")
//  //      val resultJson = contentAsJson(home)
//  //      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//  //    }
//  //
//  //    "render the appSummary resource from the router" in {
//  //      val request = FakeRequest(GET, "/api/summary")
//  //      val home = route(app, request).get
//  //
//  //      status(home) mustBe OK
//  //      contentType(home) mustBe Some("application/json")
//  //      val resultJson = contentAsJson(home)
//  //      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//  //    }
//  //  }
//}
