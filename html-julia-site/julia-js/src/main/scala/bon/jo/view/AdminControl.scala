package bon.jo.view

import bon.jo.Logger
import bon.jo.SiteModel.{MenuItem, SiteElement}
import bon.jo.app.Response
import bon.jo.app.service.DistantService
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.html.DomShell.ExtendedElement
import bon.jo.service.Raws.ImageRawExport
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.Event

import scala.concurrent.Future
import scala.scalajs.js
import scala.xml.{Node, NodeBuffer}

trait AdminControl[A <: SiteElement] extends ValueView[A] {
  type Conc = A

  def service: DistantService[A, _]

  val siteService: SiteService

  import siteService.executionContext

  val admin = siteService.user.role.admin

  def chooseMenuView: ValueView[MenuItem] with ParentComponent[Div]

  def id: String

  def value: A


  case class AdminCtx(
                       moveDiv: Ref[Span],
                       saveDiv: Ref[Span],
                       choiceDiv: Ref[Span],
                       deleteDiv: Ref[Span],
                       imgListDiv: Ref[Div],
                       adminDef: Ref[Div],
                       aShow: Ref[Div]
                     )

  def createCtx: AdminCtx = {
    AdminCtx(
      Ref[Span]("move-" + id),
      Ref[Span]("save-" + id),
      Ref[Span]("choice-" + id),
      Ref[Span]("delete-" + id),
      Ref[Div]("list-img-" + id),
      Ref[Div]("admin-configure-" + id),
      Ref[Div]("admin-show" + id)
    )

  }

  var currentCtx: AdminCtx = createCtx

  def moveDiv: Ref[Span] = currentCtx.moveDiv

  def saveDiv: Ref[Span] = currentCtx.saveDiv

  def choiceDiv: Ref[Span] = currentCtx.choiceDiv

  def deleteDiv: Ref[Span] = currentCtx.deleteDiv

  def imgListDiv: Ref[Div] = currentCtx.imgListDiv

  def adminDef: Ref[Div] = currentCtx.adminDef

  def aShow: Ref[Div] = currentCtx.aShow

  def modifyView: Node

  def adminXmlOption: Option[Node] = if (admin) Some(adminXml) else None

  def inAdmin: NodeBuffer = <span id={"move-" + id} class="btn">Move</span> <span id={"choice-" + id}></span>
    <span id={"save-" + id} class="btn save-span">
      <img width="50em" src="/julia/assets/image/save.png" alt="save"/>
    </span>
    <span id={"delete-" + id} class="btn">
      <img alt="delete"/>
    </span>

  def adminXml: Node = <div class="admin" id={"admin-" + id}>
    <img id={"admin-configure-" + id} class="img-configure" src="/julia/assets/image/configure.svg"/>
    <div id={"admin-show" + id} class="admin-show">
      {inAdmin}{modifyView}
    </div>
  </div>

  def removeFromView(): Unit


  def notInDom() = {
    currentCtx = null
  }

  def updateCtx() = {
    if (currentCtx == null) {
      currentCtx = createCtx
    }
  }

  def initAdminEvent(): Unit = {
    updateCtx()
    if (admin) {
      saveDiv.ref.addEventListener("click", (e: Event) => {
        service.update(value) map (_ => {
          saveDiv.ref.style.display = "none"
        })


      })
      deleteDiv.ref.addEventListener("click", (e: Event) => {
        service.delete(value.id) map (_ => {
          siteService.siteModel.remove(value)
          removeFromView()
        })

      })


      aShow.ref.style.display = "none"
      val click = adminDef.ref.clkOnce();
      var in = true
      click.suscribe(e => {
        if (in) {
          aShow.ref.style.display = "inline-block"
          in = false
        } else {
          in = true
          aShow.ref.style.display = "none"
        }

      })
      moveDiv.ref.addEventListener("click", (e: Event) => {
        chooseMenuView.addTo(choiceDiv.ref)
      })

    }

  }
}
