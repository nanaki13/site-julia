package bon.jo

import java.io.InputStream
import java.net.URLEncoder

import bon.jo.juliasite.pers.RepoFactory

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

object DownloadImage extends App {
  import scala.concurrent.ExecutionContext.Implicits._

  val repo = new RepoFactory().PostgresRepo
  val db = repo.db

  import repo.profile.api._

  @tailrec
  def dowload(ee: (Int, String, Array[Byte], String, String), afterTry: List[String] = List("jpg", "JPG"), i: Int = 0): Unit = {
    val start = ee._4.substring(0, ee._4.lastIndexOf('/'))
    val fenc = ec(ee._4.substring(ee._4.lastIndexOf('/') + 1))
    val end = start + "/" + fenc
    val s = ee._5 + end + "." + ee._2.substring(ee._2.lastIndexOf('/') + 1)
    val con = new java.net.URL(s)
    val stream: Option[InputStream] = None
    Try {
      val stream = con.openStream()
      val b = stream.readAllBytes()
      Await.result(db.run(repo.images.filter(e => e.id === ee._1).map(_.imgData).update(b)), Duration.Inf)
    } match {

      case Success(value) => println("ok"); stream.foreach(_.close());
      case Failure(exception) => println(exception);
        stream.foreach(_.close());
        {
          val nt = (ee._1, afterTry(i), ee._3, ee._4, ee._5)
          dowload(nt, afterTry, i + 1)
        }
    }
  }

  // (id, contentType,imgData,name,base )
  val f = db.run(repo.images.result.map { e => {
    e.map {
      ee =>
        dowload(ee)
    }
  }
  })

  def ec(s: String) = URLEncoder.encode(s, "UTF-8")
    .replaceAll("\\+", "%20")
    .replaceAll("\\%21", "!")
    .replaceAll("\\%27", "'")
    .replaceAll("\\%28", "(")
    .replaceAll("\\%29", ")")
    .replaceAll("\\%7E", "~");
  Await.result(f, Duration.Inf)
}
