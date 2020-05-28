package bon.jo.view

import bon.jo.Logger
import bon.jo.html.DomShell.$
import bon.jo.html.Types.ParentComponent
import bon.jo.html.{DomShell, InDom}
import bon.jo.phy.Obs
import bon.jo.service.Raws.ImageRawExport
import bon.jo.service.SiteService
import org.scalajs.dom.File
import org.scalajs.dom.html.{Div, Form, Input, Label}
import org.scalajs.dom.raw.{Event, FormData, HTMLElement}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.dynamics
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.xml.Node

@JSExportTopLevel("SendImage")
@JSExportAll
class SendImage(override val id: String, imageService: ImageService)(implicit  ex : ExecutionContext) extends ParentComponent[Form] {
  override def xml(): Node = {
    <form id={id} class="container">
      <div class="file-send btn">
        {DomShell.inputXml(name = "img-send", label = "+", _type = "file")}
      </div>
    </form>
  }

  def myHtml(): Form = html()

  val result : Obs[ImageRawExport] =Obs.get[ImageRawExport](id+"-result")

  var fileData: Option[File] = None

  override def init(parent: HTMLElement): Unit = {
    super.init(parent)

    //One way
    //    var event = new EventAdder( $[Input]("img-send"))
    //    event.change().obs.suscribe(e=> Logger.log(e))

    //Other way
    val changeObs: Obs[Event] = Obs.once[Event] { e: Event =>
      val target  =e.target.asInstanceOf[Input]
      val f = e.target.asInstanceOf[Input].files
      if( f != null && f .length > 0){
        fileData = Some(f(0))
        $[Label]("l-img-send").innerText = f(0).name
        $[Div]("img-send-arrow").classList.add("mirror")
      }

    }
    val clickObs: Obs[Event] = Obs.once[Event] { e: Event =>
      Logger.log(e)
      onSubmit()
    }
    val event = EventAdder($[Input]("img-send"), changeObs)

    event.change()
    val eventSubmite = EventAdder($[Input]("send"), clickObs)
    eventSubmite.click()
  }

  val send = InDom[Div](<div class="btn" id="send" ><div id="img-send-arrow" class="l-arrow"></div></div>)


  add(send)


  def onSubmit() {
    fileData foreach {
      theFile =>
        val formData = new FormData();
        formData.append("file", theFile);
        formData.append("image_name", theFile.name);
      js.special.debugger()
        this.imageService(formData).foreach(result.newValue)
        $[Div]("img-send-arrow").classList.remove("mirror")
        $[Label]("l-img-send").innerText = "+"
    }


  }

  case class EventAdder(el: HTMLElement, obs: Obs[Event] = Obs.once[Event]()) extends scala.Dynamic {


    def applyDynamic(method: String)(): this.type = {

      el.addEventListener(method, obs.newValue)
      this

    }
  }

}
class SendImageImpl(override val id : String,siteService: SiteService)(implicit  ex : ExecutionContext) extends SendImage(id,(fd)=> {

  fd.append("id",siteService.imageService.newId)
  siteService.imageService.post(fd)
})

trait ImageService extends (FormData => Future[ImageRawExport])
