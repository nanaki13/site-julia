package bon.jo.view

import bon.jo.SiteModel.{MenuItem, SiteElement}
import bon.jo.app.service.DistantService
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.Event

import scala.xml.Node

trait AdminControl[A <: SiteElement] extends ValueView[A] {
  type Conc = A

  def service: DistantService[A, _]

  val siteService: SiteService

  import siteService.executionContext

  val admin = siteService.user.role.admin

  def chooseMenuView : ValueView[MenuItem] with ParentComponent[Div]

  def id: String

  def value: A

  val moveDiv: Ref[Span] = Ref[Span]("move-" + id)
  val saveDiv: Ref[Span] = Ref[Span]("save-" + id)
  val choiceDiv: Ref[Span] = Ref[Span]("choice-" + id)
  val deleteDiv: Ref[Span] = Ref[Span]("delete-" + id)
  val aShow: Ref[Div] = Ref[Div]({
    "admin-show" + id
  })
  val adminDef: Ref[Div] = Ref[Div]({
    "admin-" + id
  })

  def modifyView: Node

  def adminXmlOption: Option[Node] = if (admin) Some(adminXml) else None

  def adminXml: Node = <div class="admin" id={"admin-" + id}>
    <img class="img-configure" src="/julia/assets/image/configure.svg"/>
    <div id={"admin-show" + id}>
      <span class="btn btn-primary" id={"move-" + id}>Move</span> <span id={"choice-" + id}></span>
      <span class="btn btn-primary" id={"save-" + id}>save</span>
      <span class="btn btn-primary" id={"delete-" + id}>delete</span>{modifyView}
    </div>
  </div>

  def removeFromView(): Unit


  def initAdminEvent(): Unit = {
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
      adminDef.ref.addEventListener("mouseover", (_: Event) => {
        aShow.ref.style.position = "relative"
        aShow.ref.style.top = "0"
        aShow.ref.style.display = "inline-block"
      })
      adminDef.ref.addEventListener("mouseleave", (_: Event) => {
        aShow.ref.style.display = "none"
      })
      moveDiv.ref.addEventListener("click", (e: Event) => {

        chooseMenuView.addTo(choiceDiv.ref)

      })
    }

  }
}
