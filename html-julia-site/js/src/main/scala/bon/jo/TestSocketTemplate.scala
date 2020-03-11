package bon.jo

import bon.jo.game.Const
import bon.jo.game.html.Template
import bon.jo.html.DomShell.{$, inputXml}
import bon.jo.html.OnClick
import org.scalajs.dom.html.{Div, Input}

class TestSocketTemplate extends Template {
  var msg: Div = _
  var urlInput: Input = _
  var sendInput: Input = _

  override def updateView(): Unit = {
    myButton.addTo("submit")
    msg = $[Div]("resp")
    urlInput = $[Input]("url")
    sendInput = $[Input]("send")
  }

  override def body: String = <div id="msg" class="container">
    {inputXml("url", "url", Const.urlCardGame)}{inputXml("send", "envoi")}<div id="submit">

    </div>
    <div>
      <div  id="resp" class="container">

      </div>
    </div>
  </div>.mkString

  val myButton = OnClick("send-button", "Envoi")


  def msg(str: String): Unit = {
    msg.innerHTML = s"""<span class="m-2">reponse</span><pre>"""+ str+"</pre>" + msg.innerHTML
  }

  case class Param(url: String, send: String)

  def getParam(): Param = {
    Param(urlInput.value, sendInput.value)
  }
}
