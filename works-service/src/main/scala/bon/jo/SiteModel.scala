package bon.jo

import akka.http.scaladsl.model.{HttpResponse, StatusCode}

object SiteModel {

  case class MenuItem(id: Option[Int] = None, title: String, themeKey: Option[Int] = None,x : Int, y : Int ) extends OkResponse with Position

  object MenuItem {
//    def apply(tup: (Option[Int], String, Option[Int])): MenuItem = {
//      MenuItem(tup._1, tup._2, tup._3)
//    }

    def apply(title: String) = new MenuItem(title = title,x=0,y=0)


  }

  case class ImgLinkOb(link: String) extends OkResponse

  object ImgLink {

    def apply(id: Int, contentType: String): String =
      s"image/${id}" + (contentType match {
        case "image/jpeg" => ".jpg"
        case "image/png" => ".png"
        case _ => ".jpg"
      })
  }

  trait OkResponse
  trait KoResponse {
    def status : StatusCode
  }

  trait Position{
    def x : Int
    def y : Int
  }

}