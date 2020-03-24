package bon.jo.service

import bon.jo.html.DomShell
import org.scalajs.dom.{Event, XMLHttpRequest}

import scala.scalajs.js
import scala.scalajs.js.JSON

object RequestHttp {
  def apply[R](method: Method, body: R)(response: Option[R => Unit])(st: Int => Unit)(implicit urlDesr: String,
                                                                                      writeRequest: R => js.Any,
                                                                                      readResponse: js.Any => R): RequestHttp = {
    implicit val st_ : Int => Unit = st
    implicit val bodyrequest: js.Any = writeRequest(body)
    val f: Option[js.Any => Unit] = response.map(res => {
      readResponse.andThen(r => res(r))

    })
    method.apply(f)
  }

  def apply[R](method: Method, urlDesr: String, pathSuffix: String)(response: Option[R => Unit])(st: Int => Unit)
              (implicit readResponse: js.Any => R): RequestHttp = {
    implicit val bodyrequest: js.Any = null
    implicit val st_ : Int => Unit = st
    implicit val path: String = urlDesr + pathSuffix
    val f: Option[js.Any => Unit] = response.map(res => {
      readResponse.andThen(r => res(r))

    })
    method.apply(f)

  }

  sealed abstract class Method(val okStatus: Int) {
    val name: String = this.toString

    def okStatus(status: Int): Boolean = okStatus == status

    def apply(reponseConsumer: Option[js.Dynamic => Unit], okProcess: Option[() => Unit] = None, okRawProcess: Option[String => Unit]  = None)(implicit an: js.Any, urlDesr: String, st: Int => Unit): RequestHttp = {
      this match {
        case bon.jo.service.RequestHttp.POST => new POST(reponseConsumer)
        case bon.jo.service.RequestHttp.GET => new GET(reponseConsumer = reponseConsumer, rawConsumer = okRawProcess)
        case bon.jo.service.RequestHttp.PATCH => new PATCH(reponseConsumer)
        case bon.jo.service.RequestHttp.DELETE => new DELETE(okProcess)
      }
    }
  }

  case object POST extends Method(201)

  case object GET extends Method(200) {
    def `doRaw`(url: String)(rwoConsume: String => Unit) {
      implicit val body: js.Any = null
      implicit val des: String = url
      implicit val s: Int => Unit = DomShell.log
      val get = this(None, Some(rwoConsume))
      get.prepare()
      get.send()
    }
  }

  case object PATCH extends Method(204)

  case object DELETE extends Method(204)

  case class POST(reponseConsumer: Option[js.Dynamic => Unit])(implicit an: js.Any, urlDesr: String, status: Int => Unit) extends RequestHttp(urlDesr, POST, an, reponseConsumer, Some(status), None)

  case class GET(reponseConsumer: Option[js.Dynamic => Unit], rawConsumer: Option[String => Unit] )(implicit an: js.Any, urlDesr: String, status: Int => Unit) extends RequestHttp(urlDesr, GET, an, reponseConsumer, Some(status), None, rawConsumer) {
    override def send(): Unit = {
      request.send(null)
    }


  }

  case class PATCH(reponseConsumer: Option[js.Dynamic => Unit])(implicit an: js.Any, urlDesr: String, status: Int => Unit) extends RequestHttp(urlDesr, PATCH, an, reponseConsumer, Some(status))

  case class DELETE(okProcess: Option[() => Unit])(implicit an: js.Any, urlDesr: String, status: Int => Unit) extends RequestHttp(urlDesr, DELETE, an, None, Some(status), okProcess) {
    override def send(): Unit = {
      request.send(null)
    }
  }

}


abstract sealed class RequestHttp(urlDesr: String,
                                  method: RequestHttp.Method,
                                  an: js.Any,
                                  reponseConsumer: Option[js.Dynamic => Unit],
                                  status: Option[Int => Unit] = None,
                                  okProcess: Option[() => Unit] = None,
                                  reponseRawConsumer: Option[String => Unit] = None,
                                  param: Map[String, Any] = Map()) {
  val request = new XMLHttpRequest

  def open(): Unit = request.open(method.name, urlDesr)

  def send(): Unit = request.send(JSON.stringify(an))

  def contentString(r: XMLHttpRequest): String = {
    request.response.toString
  }

  def responseJson(r: XMLHttpRequest): Unit = reponseConsumer.foreach(_ (JSON.parse(r.response.toString)))

  def responseRaw(r: XMLHttpRequest): Unit = reponseRawConsumer.foreach(_ (r.response.toString))

  def statusConsumer(r: Int): Unit = status.foreach(_ (r))

  def nonOkProcess(): Unit = DomShell.log(s"erreur sending $an to $urlDesr")

  def okStatus(status: Int): Boolean = method.okStatus(status)

  def prepare(): Unit = {
    open()
    //Envoie les informations du header adaptées avec la requête
    request.setRequestHeader("Content-Type", "application/json");
    request.onreadystatechange = (e: Event) => { //Appelle une fonction au changement d'état.
      if (request.readyState == XMLHttpRequest.DONE) {
        statusConsumer(request.status)
        if (okStatus(request.status)) {
          okProcess.foreach(_ ())
          responseJson(request)
          responseRaw(request)
        } else {
          nonOkProcess()
        }
      }
    }

  }
}