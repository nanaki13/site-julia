package bon.jo.view

import bon.jo.SiteModel.SiteElement
import bon.jo.html.{IdView, InDom}
import bon.jo.service.Raws.ImageRawExport
import bon.jo.view.Lists.PagChooseList
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.html.{Image, Span}
import org.scalajs.dom.raw.{Event, HTMLElement}

import scala.concurrent.ExecutionContext
import scala.xml.NodeBuffer

trait WithImage[A <: HTMLElement, B <: SiteElement[ID],ID] extends InDom[A] with AdminControl[B,ID] with intOnce with IdView {

  implicit val executionContext: ExecutionContext
  protected var _imgRef: Option[Ref[Image]] = None

  def imgRef :  Option[Ref[Image]] = {
    if (_imgRef.isEmpty){
      _imgRef = factory
    }
    _imgRef
  }
  override def notInDom(): Unit = {
    super.notInDom()
    _imgRef = None
  }
  def factory: Option[Ref[Image]]

  def image: Option[SiteModel.Image]
  def imageFor(e: ImageRawExport):Unit
  val imgDiv: Ref[Span] = Ref[Span]("img-" + id)

  override def inAdmin: NodeBuffer = {
    {super.inAdmin}.addOne(<span id={"img-" + id} class="btn">
      <img alt="img"/>
    </span>)
  }


  def updateSrc(e : SiteModel.Image): Unit = imgRef.foreach(_.ref.src = e.base + e.link)
  def updateSrc(e : String): Unit = imgRef.foreach(_.ref.src = e)

  def choiceIsDone(listImg : PagChooseList[ImageRawExport, ImgView] ,imageRawExport: ImageRawExport)={
    listImg.removeFromView()
    siteService.showAll
    imageFor(imageRawExport)
  }
  def initImg(parent: HTMLElement): Unit = {

    if(admin){
      imgDiv.ref.addEventListener("click", (e: Event) => {
        val listImg: PagChooseList[ImageRawExport, ImgView] = Lists.PagChooseList[ImageRawExport, ImgView]("img-List-" + id, "container", _.iRaw,rebuildp =
          e => ImgView(e)

          ,addElementp = None)
        def choiceDon(e : ImageRawExport ): Unit = choiceIsDone(listImg,e)
        listImg.obs.suscribe(choiceDon)

        siteService.hideAll
        siteService.root(listImg)

        listImg.me.classList.add("overall")
        //    listImg.loading()
        siteService.imageService.getAll.foreach {
          e =>
            def elts: List[ImgView] = e.map { iRaw =>
              ImgView("img-sm-" + iRaw.id.toString, "img-sm", iRaw)
            }.toList

            listImg.clearAndAddAll(elts)
              val send = new SendImageImpl("img-send-test",siteService )
            listImg.addAtEnd(send)
            send.result.suscribe(choiceDon)


        }
      })
    }

    image match {
      case Some(value) =>

        imgRef.get.ref.addEventListener("load", (e: Event) => {
          Logger.log("Image loaded");
          imgRef.get.ref.classList.remove("loader")

        });
        imgRef.get.ref.classList.add("loader")

        updateSrc(value)
      case None =>  updateSrc("/julia/assets/image/configure.svg")
    }
  }
}
