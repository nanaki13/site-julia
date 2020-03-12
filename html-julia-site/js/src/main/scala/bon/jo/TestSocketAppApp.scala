package bon.jo

import bon.jo.app.HtmlApp
import bon.jo.game.html.Template
import bon.jo.util.SocketKeeper
import bon.jo.util.SocketKeeper.SocketContext
import org.scalajs.dom.html.Div

class TestSocketAppApp(app: Div, template: Template) extends HtmlApp[TestSocketTemplate](app: Div, template: Template) {

}
