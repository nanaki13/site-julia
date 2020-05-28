package bon.jo.view

import java.util.concurrent.TimeUnit

import bon.jo.Logger
import bon.jo.SiteModel.{Oeuvre, SiteElement, ThemeMenuItem}
import bon.jo.app.service.DistantService
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.ParentComponent
import bon.jo.html.ValueView
import bon.jo.service.SiteService
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.Event

import scala.concurrent.duration.FiniteDuration
import scala.xml.{Node, NodeBuffer}

trait AdminControl[A <: SiteElement[ID],ID] extends ValueView[A] {
  type Conc = A

  def service: DistantService[A, _, ID]

  val siteService: SiteService

  import siteService.executionContext

  val admin = siteService.user.role.admin

  def chooseMenuView: ValueView[ThemeMenuItem] with ParentComponent[Div]

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
        val v = value
        Logger.log(v.asInstanceOf[Oeuvre].image.toString + "--- ")
        Logger.log(asInstanceOf[WithImage[_, _,_]].image.toString + "--- ")
        service.update(v) map (_ => {

          obs.newValue(v)
          // saveDiv.ref.style.display = "none"
        })


      })
      deleteDiv.ref.addEventListener("click", (e: Event) => {
        val v = value
        service.delete(v.id) map (_ => {
          siteService.siteModel.remove(v)
          removeFromView()
        })

      })



      val click = adminDef.ref.clkOnce();
      var in = true
      click.suscribe(e => {
        if (in) {
        //  $[Div]("admin-"+id).classList.add("admin-clicked" )
          $[Div]("admin-"+id).style.top="15em";
          $[Div]("admin-"+id).style.left="20%";
          $[Div]("admin-"+id).style.position="fixed";
          $[Div]("admin-"+id).style.zIndex="1000";
         // aShow.ref.style.display = "inline-block"
       //   adminDef.ref.classList.add("img-configure-clicked")
       //   adminDef.ref.classList.remove("img-configure")

          adminDef.ref.style.top="-2em";
          aShow.ref.style.display = "inline-block"
          aShow.ref.style.opacity = "1"
          aShow.ref.style.width = "35em"
          aShow.ref.style.height = "auto"
          in = false
        } else {
          in = true
      //    $[Div]("admin-"+id).classList.remove("admin-clicked" )
          $[Div]("admin-"+id).style.top=null;
          $[Div]("admin-"+id).style.left=null
          $[Div]("admin-"+id).style.position="absolute";
          $[Div]("admin-"+id).style.zIndex="1000";

          adminDef.ref.style.top="0";

       //   adminDef.ref.classList.remove("img-configure-clicked")
 //         adminDef.ref.classList.add("img-configure")
          aShow.ref.style.opacity = "0"
          aShow.ref.style.width = "0"
          aShow.ref.style.height = "0"
          scalajs.js.timers.setTimeout(FiniteDuration(750,TimeUnit.MILLISECONDS)){
            aShow.ref.style.display = "none"
          }
        }

      })
      moveDiv.ref.addEventListener("click", (e: Event) => {
        chooseMenuView.addTo(choiceDiv.ref)
      })

    }

  }
}
