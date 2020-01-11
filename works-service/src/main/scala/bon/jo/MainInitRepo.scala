package bon.jo

import bon.jo.juliasite.pers.PostgresRepo

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MainInitRepo extends App {
  implicit val ec = scala.concurrent.ExecutionContext.global
   Await.result(PostgresRepo.Initilaizer.createDropCreate(),Duration.Inf)
}
