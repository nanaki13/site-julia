package bon.jo

import akka.http.scaladsl.model.{HttpResponse, StatusCode}

object SiteModelService {

 // case class MenuItem(id: Option[Int] = None, title: String, themeKey: Option[Int] = None, x: Int, y: Int,image: Option[ImgLinkOb]= None, `type` : String) extends OkResponse with Position

  // object MenuItem {
    //    def apply(tup: (Option[Int], String, Option[Int])): MenuItem = {
    //      MenuItem(tup._1, tup._2, tup._3)
    //    }

   // def apply(title: String) = new MenuItem(title = title, x = 0, y = 0)


  //  }

  trait KoResponse {
    def status: StatusCode
  }
}