package bon.jo

import akka.http.scaladsl.server.{HttpApp, Route}

import scala.concurrent.ExecutionContext


// Server definition
object WebServer extends HttpApp {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  override def routes: Route = Routes.allRoutes


}




