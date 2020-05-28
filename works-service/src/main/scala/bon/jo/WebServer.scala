package bon.jo

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.{ExceptionHandler, HttpApp, Route}
import bon.jo.juliasite.pers.RepoFactory
import bon.jo.service.ServicesFactory

import scala.concurrent.ExecutionContextExecutor


object WebServer extends HttpApp {

  implicit val ex: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Exception =>
        e.printStackTrace()
        complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
    }

  val repo = RepoFactory()(scala.concurrent.ExecutionContext.global)
  val servies = new ServicesFactory(repo.PostgresRepo)
  val routesFromServices = new Routes(servies.servies)

  override def routes: Route = Route.seal(routesFromServices.allRoutes)

}




