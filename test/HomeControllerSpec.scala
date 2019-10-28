package controllers

import bon.jo.helloworld.juliasite.pers.H2Repo
import controllers.SiteModel.MenuItem
import controllers.services.Services.{ImageService, MenuService, SericeImpl, Service}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {


  trait serviceAll extends Service with ImageService with MenuService {
    override  val dbc = dbConntext
  }
  object serviceImpl extends SericeImpl(H2Repo) with serviceAll

  "service can createz" should {

    "create a root thme" in {
      import serviceImpl.ctx
      val controller = new HomeController(stubControllerComponents(), serviceImpl, serviceImpl)
      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      val resultJson = contentAsJson(home)
      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""

      val ad = controller.addRootTheme(MenuItem(None,"test",None))
      status(ad) mustBe OK
      contentType(ad) mustBe Some("application/json")
      println( contentAsJson(ad))
    }
  }

//  "HomeController GET" should {
//
//    "render the appSummary resource from a new instance of controller" in {
//
//      val controller = new HomeController(stubControllerComponents(),serviceImpl,serviceImpl)
//      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))
//
//      status(home) mustBe OK
//      contentType(home) mustBe Some("application/json")
//      val resultJson = contentAsJson(home)
//      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//    }
//
//    "render the appSummary resource from the application" in {
//      val controller = inject[HomeController]
//      val home = controller.appSummary().apply(FakeRequest(GET, "/summary"))
//
//      status(home) mustBe OK
//      contentType(home) mustBe Some("application/json")
//      val resultJson = contentAsJson(home)
//      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//    }
//
//    "render the appSummary resource from the router" in {
//      val request = FakeRequest(GET, "/api/summary")
//      val home = route(app, request).get
//
//      status(home) mustBe OK
//      contentType(home) mustBe Some("application/json")
//      val resultJson = contentAsJson(home)
//      resultJson.toString() mustBe """{"content":"Scala Play Angular Seed"}"""
//    }
//  }
}
