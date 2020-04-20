package bon.jo.view

import bon.jo.SiteModel.SiteElement
import bon.jo.html.{DomShell, IdView, InDom}
import bon.jo.service.Raws.ImageRawExport
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.html.{Image, Span}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.xml.NodeBuffer

trait WithImage[A <: HTMLElement, B <: SiteElement] extends InDom[A] with AdminControl[B] with intOnce with IdView {

  implicit val executionContext: ExecutionContext
  private var _imgRef: Option[Ref[Image]] = None

  def imgRef :  Option[Ref[Image]] = {
    if (_imgRef.isEmpty){
      _imgRef = Some(factory)
    }
    _imgRef
  }
  override def notInDom(): Unit = {
    super.notInDom()
    _imgRef = None
  }
  def factory: Ref[Image]

  def image: Option[SiteModel.Image]
  def imageFor(e: ImageRawExport):Unit
  val imgDiv: Ref[Span] = Ref[Span]("img-" + id)

  override def inAdmin: NodeBuffer = {
    {super.inAdmin}.addOne(<span id={"img-" + id} class="btn">
      <img alt="img"/>
    </span>)
  }


  def updateSrc(e : SiteModel.Image): Unit ={ imgRef.get.ref.src = e.base + e.link}

  def initImg(parent: HTMLElement): Unit = {

    if(admin){
      imgDiv.ref.addEventListener("click", (e: Event) => {
        val listImg = Lists.PagChooseList[ImageRawExport, ImgView]("img-List-" + id, "container", _.imageRawExport)
        listImg.obs.suscribe(e => {
          listImg.removeFromView()
          siteService.showAll
          imageFor(e)

        })

        siteService.hideAll
        siteService.root(listImg)

        listImg.me.classList.add("overall")
        //    listImg.loading()
        siteService.imageService.getAll.foreach {
          e =>
            def elts: List[ImgView] = e.map { iRaw =>
              ImgView("img-sm-" + iRaw.id.toString, iRaw.base + "/" + iRaw.link, "img-" + iRaw.id, "img-sm", iRaw)
            }.toList

            listImg.clearAndAddAll(elts)
              val send = new SendImageImpl("img-send-test",siteService )
            listImg.addAtEnd(send)


        }
      })
    }

    image.foreach(e => {
//      if (notInit) {
        notInit = false
        imgRef.get.ref.addEventListener("load", (e: Event) => {
          Logger.log("Image loaded");
          imgRef.get.ref.classList.remove("loader")

        });
        imgRef.get.ref.classList.add("loader")
//      }
      updateSrc(e)
    })


  }
}
