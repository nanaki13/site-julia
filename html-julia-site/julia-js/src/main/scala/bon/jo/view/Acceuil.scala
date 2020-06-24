package bon.jo.view

import java.util.concurrent.TimeUnit

import bon.jo.SiteModel.Image
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.$
import bon.jo.html.Types._Div
import bon.jo.phy.MemoObs
import bon.jo.service.Raws.ImageRawExport
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.window

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.xml.Elem

class Acceuil(imageService: DistantService[Image, ImageRawExport, Int])(implicit executionContext: ExecutionContext) extends _Div {


  var viewImage1: ImgView = _
  var viewImage2: ImgView = _
  var swotch = true
  var srcs: List[ImageRawExport] = Nil
  val images = new MemoObs[List[ImageRawExport]]

  imageService.getAll.map(_.toList).map(images.newValue)

  def addImages(value: List[ImageRawExport]): Unit = {
    srcs = value
    viewImage1 = ImgView("im-v-a-1", "img-acc", imgClass = "img-acc-img")
    viewImage1.addTo(me)
    viewImage2 = ImgView("im-v-a-2", "img-acc", imgClass = "img-acc-img")
    viewImage2.addTo(me)

    me.style.position = "absolute"
    val calcWidth =  window.innerWidth - $[Div]("side-menu").getBoundingClientRect().right - $[Div]("side-menu").getBoundingClientRect().left

    val (w, h) = ( calcWidth*0.97,window.innerHeight*0.97)
    val (wIn, hIn) = ( calcWidth*0.95,window.innerHeight*0.95)
    val (ws, hs) = (w.toString+"px",h.toString+"px")
    val (wsIn, hsIn) = (wIn.toString+"px",hIn.toString+"px")
   // viewImage1.img.ref.style.maxHeight =hsIn
    //viewImage2.img.ref.style.maxHeight = hsIn
    viewImage1.img.ref.style.width =wsIn
    viewImage2.img.ref.style.width = wsIn

    viewImage1.me.style.width =ws
    viewImage2.me.style.width = ws
    viewImage1.me.style.height =hs
    viewImage2.me.style.height = hs
    viewImage1.me.style.left = ((w - wIn)/2).toString+"px"
    viewImage2.me.style.left = ((w - wIn)/2).toString+"px"
    viewImage1.me.style.top = ((h - hIn)/2).toString+"px"
    viewImage2.me.style.top = ((h - hIn)/2).toString+"px"
    scalajs.js.timers.setInterval(Duration(5, TimeUnit.SECONDS))({
      val (in, out) = if (swotch) {
        (viewImage1, viewImage2)
      } else {
        (viewImage2, viewImage1)
      }
      swotch = !swotch
      in.img.ref.src = srcs.head.base + srcs.head.link
      srcs = srcs.tail :+ srcs.head

      scalajs.js.timers.setTimeout(Duration(1, TimeUnit.SECONDS))({
        out.img.ref.style.opacity = "0";
        scalajs.js.timers.setTimeout(Duration(500, TimeUnit.MILLISECONDS))({
          in.img.ref.style.opacity = "1"
        })
      })
    })
  }

  override def init(parent: HTMLElement): Unit = {
    images.suscribe(addImages)
  }

  override def idXml: Elem = <div>

  </div>
}
