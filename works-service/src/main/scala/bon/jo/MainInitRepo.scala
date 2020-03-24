package bon.jo

import bon.jo.juliasite.pers.RepoFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object MainInitRepo extends App {

  import scala.concurrent.ExecutionContext.Implicits._

  val repo = new RepoFactory().PostgresRepo
  val db = repo.db
  repo.dropAll().foreach(e => {
    val f = db.run(e.asTry).map {
      case Success(s) => s
      case Failure(exception) => println(exception)
    }
    Await.result(f, Duration.Inf)

  })
  Await.result(repo.createMissing(), Duration.Inf)
}
