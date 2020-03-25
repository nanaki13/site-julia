package bon.jo.service

import bon.jo.app.RequestHttp
import bon.jo.html.DomShell

import scala.scalajs.js

case class DistantService[A](url: String)
                            (implicit read: js.Any => A, wrie: A => js.Any) {

  def bodyRequestFactory(body: A)(method: RequestHttp.Method): Option[(A => Unit)] => (Int => Unit) => RequestHttp = {
    implicit val u: String = url
    RequestHttp.apply[A](method, body)
  }

  def pathRequestFactory(pathSuffix: String)(method: RequestHttp.Method): Option[A => Unit] => (Int => Unit) => RequestHttp = {
    implicit val u: String = url
    RequestHttp.apply[A](method, url, pathSuffix)
  }

  def noBodyPathRequest(pathSuffix: String)(method: RequestHttp.Method): (Int => Unit) => RequestHttp = {
    implicit val u: String = url
    RequestHttp.apply[A](method, url, pathSuffix)(None)
  }

  val postFacotory: A => Option[A => Unit] => (Int => Unit) => RequestHttp = bodyRequestFactory(_: A)(RequestHttp.POST)
  val patchFacotory: A => Option[A => Unit] => (Int => Unit) => RequestHttp = bodyRequestFactory(_: A)(RequestHttp.PATCH)
  val getFacotory: String => Option[A => Unit] => (Int => Unit) => RequestHttp = { e => pathRequestFactory(e)(RequestHttp.GET) }
  val deleteFacotory: String => (Int => Unit) => RequestHttp = { e => noBodyPathRequest(e)(RequestHttp.DELETE) }

  def saveLogReturn(m: A): Unit = {
    val req = postFacotory(m)(Some(resp => {
      console(resp)
    }))(console)
    req.prepare()
    req.send()
  }

  def save(m: A)(ret: A => Unit): Unit = {
    val req = postFacotory(m)(Some(ret))(console)
    req.prepare()
    req.send()
  }

  def update(m: A)(ok: => Unit): Unit = {
    val req = patchFacotory(m)(None)((c: Int) => {
      if (RequestHttp.PATCH.okStatus(c)) {
        ok
      }
    })
    req.prepare()
    req.send()
  }

  def get(id: Int)(isGet: A => Unit): Unit = {
    val req = getFacotory(id.toString)(Some(isGet))(console)
    req.prepare()
    req.send()
  }

  def delete(id: Int)(ok: () => Unit = () => {}): Unit = {
    val req = deleteFacotory(id.toString)(console.compose(e => ok()))
    req.prepare()
    req.send()
  }

  def console = DomShell.log _
}
