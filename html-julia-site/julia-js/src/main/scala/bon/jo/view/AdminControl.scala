package bon.jo.view

import bon.jo.SiteModel.SiteElement
import bon.jo.service.{DistantService, SiteService}
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.Event

import scala.xml.Node

trait AdminControl[A <: SiteElement] {
  type Conc = A
  def service : DistantService[A]
  def siteService : SiteService
  def id : String
  def value : A
   val moveDiv: Ref[Span] = Ref[Span]("move-" + id)
   val saveDiv: Ref[Span] = Ref[Span]("save-" + id)
   val choiceDiv: Ref[Span] = Ref[Span]("choice-" + id)
   val deleteDiv: Ref[Span] = Ref[Span]("delete-" + id)
   val aShow: Ref[Div] = Ref[Div]({
    "admin-show" + id
  })
   val admin: Ref[Div] = Ref[Div]({
    "admin-" + id
  })
  def modifyView: Node
  def adminXml: Node = <div id={"admin-" + id}>
    <img width="20em" height="20em" src="/julia/assets/image/configure.svg"/>
    <div id={"admin-show" + id}>
      <span class="btn btn-primary" id={"move-" + id}>Move</span> <span id={"choice-" + id}></span>
      <span class="btn btn-primary" id={"save-" + id}>save</span>
      <span class="btn btn-primary" id={"delete-" + id}>delete</span>
      {modifyView}
    </div>
  </div>

  def removeFromView(): Unit

  import scala.concurrent.ExecutionContext.Implicits._
  def initAdminEvent(): Unit ={
    saveDiv.ref.addEventListener("click", (e: Event) => {
      service.update(value)(ok =   {
        saveDiv.ref.style.display = "none"
      })


    })
    deleteDiv.ref.addEventListener("click", (e: Event) => {
      service.delete(value.id) map  (_ => {
        siteService.siteModel.remove(value)
        removeFromView()
      })

    })
    aShow.ref.style.display = "none"
    admin.ref.addEventListener("mouseover", (_: Event) => {
      aShow.ref.style.display = "inline-block"
    })
    admin.ref.addEventListener("mouseleave", (_: Event) => {
      aShow.ref.style.display = "none"
    })
  }
}
