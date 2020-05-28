package bon.jo.app

import bon.jo.app.RequestHttp.GET
import bon.jo.game.html.Template
import bon.jo.html.{ DomShell, OnClick}
import bon.jo.view.Ref
import org.scalajs.dom.html.{Div, Input}
import org.scalajs.dom.raw.HTMLElement

class LoginTemplate(override val user: User) extends Template {
  implicit val ex = scala.concurrent.ExecutionContext.global

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
  val commt = OnClick("ok-btn", "ok")



  commt.obsClick().suscribe( _ => {

    val des = s"/auth?login=${l.value}&pwd=${p.value}"

    GET.send(des) map {
      resp =>
        resp.bodyAsString.map({
          token => {
            val red = s"/julia/index.html?token=$token"
            org.scalajs.dom.window.location.assign(red)
          }
        })
    }
  })

  val btnDiv: Ref[Div] = Ref[Div]("ok")

  override def init(parent: HTMLElement): Unit = {
    commt.addTo(btnDiv.ref)
    commt.init(btnDiv.ref)
  }
}
