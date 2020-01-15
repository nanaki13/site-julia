package bon.jo

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.{ExceptionHandler, HttpApp, Route}
import bon.jo.Routes.complete

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal


// Server definition
object WebServer extends HttpApp {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case  e : Exception =>
        e.printStackTrace()
        complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
    }
  override def routes: Route = Route.seal(Routes.allRoutes)

}




