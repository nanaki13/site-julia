package bon.jo

import bon.jo.game.html.Template
import bon.jo.html.{ButtonHtml, DomShell}
import bon.jo.service.SiteService
import bon.jo.view.SiteModelView
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.JSON

class TestSocketTemplate extends Template {
  var service : SiteService = _


  private var _site: SiteModelView = _

  def site_=(s : SiteModelView): Unit ={

    _site = s
  }
  def site: SiteModelView = _site
  override def updateView(): Unit = {
    _site.updateView()



  }

  val button = ButtonHtml("btn-export","export")
  override def body: String = (<div id="root"><div id="ex"></div></div>).mkString

  override def init(p : HTMLElement): Unit = {
    button.onClick(_ => {
      DomShell.log("click export")
      val s : String = JSON.stringify( service.`export`)
      DomShell.deb()
      DomShell.log(service.`export`)
    })
    button.addTo("ex")

    _site.init(me)

  }
}
