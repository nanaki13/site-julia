package bon.jo

import bon.jo.juliasite.pers.PostgresRepo

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object MainInitRepo extends App {
  implicit val ec = scala.concurrent.ExecutionContext.global
  val db = PostgresRepo.db
   PostgresRepo.dropAll().foreach(e => {
     val f = db.run(e.asTry).map{
       case Success(s)=>s
       case Failure(exception) => println(exception)
     }
     Await.result(f,Duration.Inf)

   } )
   Await.result(PostgresRepo.createMissing(),Duration.Inf)
}
