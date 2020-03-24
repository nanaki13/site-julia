package bon.jo.app

import java.util.Base64

import bon.jo.game.html.Template
import bon.jo.html.{ButtonHtml, DomShell}
import bon.jo.service.RequestHttp.GET
import bon.jo.view.Ref
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js.JSON

class LoginTemplate extends Template {
  override def body: String =
    <div class="container-fluid p-5">
      <div class="container  bg-primary rounded p-2 mw-50">
        {DomShell.form(
        DomShell.inputXml("login", "login"),
        DomShell.inputXml(name = "password", label = "password", _type = "password")
      )}<div id="ok"></div>
      </div>


    </div>.mkString

  val l = Ref[Input]("login")
  val p = Ref[Input]("password")
  val commt = ButtonHtml("ok-btn", "ok")

  def parse(resp: String): Unit = {
    val playLoad = JSON.parse(new String(Base64.getUrlDecoder.decode(resp.substring(resp.indexOf('.') + 1, resp.lastIndexOf('.')))))
    DomShell.log(playLoad)
  }

  commt.onClick(_ => {

    val des = s"/auth?login=${l.value}&pwd=${p.value}"

    GET.`doRaw`(des) {
      resp =>
        parse(resp)
    }

  })

  val btnDiv: Ref[Div] = Ref[Div]("ok")

  override def init(parent: HTMLElement): Unit = {
    commt.addTo(btnDiv.ref)
    commt.init(btnDiv.ref)
  }
}
