package bon.jo.view

import bon.jo.Logger
import bon.jo.SiteModel.{Text, TextId}
import bon.jo.app.User
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.FinalComponent
import bon.jo.phy.Obs
import bon.jo.service.TextService
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.xml.Elem

case class SiteTitle(textService: TextService)(implicit val executionContext: ExecutionContext, user: User) extends FinalComponent[Div] {

  case class Title(title: String, subTitle: String)

  val TITLE = 0
  val SUB_TITLE = 1
  val title: Obs[Title] = Obs.once()
  title.suscribe(v => {
    update(v)
  })

  def update(v: Title): Unit = {
    $[Div]("title").innerText = v.title
    $[Div]("sub-title").innerText = v.subTitle
  }

  def updateTitle(s: String): Unit = {
    textService.update(Text(TextId("title", TITLE), s)).onComplete{
      case Failure(exception) => PopUp("Le titre n'a pu être sauvegarder");Logger.log(exception)
      case _ => PopUp("Element sauvegarder")
    }
  }

  def updateSubTitle(s: String): Unit = {
    textService.update(Text(TextId("title", SUB_TITLE), s)).onComplete{
      case Failure(exception) => PopUp("Le sous titre n'a pu être sauvegarder");Logger.log(exception)
      case _ => PopUp("Element sauvegarder")
    }
  }


  override def xml(): Elem = <div id={id}>
    <div id="title" class="title_main"></div>
    <div id="sub-title"></div>
  </div>

  override def id: String = "site-title"

  override def init(parent: HTMLElement): Unit = {
    textService.getByUid("title") map {
      e =>
        if (e.isEmpty) {
          val ti: Text = Text(TextId("title", TITLE), "Julia Le Corre")
          val su: Text = Text(TextId("title", SUB_TITLE), "Artiste")
          textService.save(ti)
          textService.save(su)
          Title(ti.text, su.text)
        } else {
          Title(e.find(_.id.index == TITLE).get.text, e.find(_.id.index == SUB_TITLE).get.text)
        }
    } foreach (title.newValue)
    if (user.role.admin) {
      $[Div]("title").UserCanUpdate()(() => Obs.once()).suscribe(updateTitle)
      $[Div]("sub-title").UserCanUpdate()(() => Obs.once()).suscribe(updateSubTitle)
    }

  }
}
