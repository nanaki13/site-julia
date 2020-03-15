package bon.jo.view

import bon.jo.SiteModel
import bon.jo.SiteModel.MenuItem
import bon.jo.html.DomShell.$
import bon.jo.html.DomShell.ExtendedElement
import bon.jo.html.Types.{Clickable, FinalComponent, ParentComponent}
import bon.jo.html._
import bon.jo.service.SiteService
import bon.jo.view.Ref.RefComp
import org.scalajs.dom.Event
import org.scalajs.dom.html.{Button, Div, Input}
import org.scalajs.dom.raw.HTMLElement

import scala.xml.{Group, Node}


case class SiteModelView(model: SiteModel)(implicit val siteService: SiteService) extends XmlHtmlView[Div] with InDom[Div] with NodeView[Div] {


  val admin = true

  implicit val siteModelView: SiteModelView = this
  val mainContent: Ref[Div] = Ref("current-view")
  val sideMdenu: Ref[Div] = Ref("side-menu")
  val addSubMenu: Ref[Div] = Ref("addSubMenu")
  val addMainMenu: Ref[Div] = Ref("addMainMenu")


  var itemsView: List[ManiMenuItemView] = model.items.map(ManiMenuItemView.apply)

  val makeNewItem: SimpleInput = SimpleInput("newItem", "nom du menu")
  val makeNewSubItem: SimpleInput = SimpleInput("newSubItem", "nom du menu")

  makeNewItem.confirm.onClick(_ => {
    val newItem = MenuItem(0, makeNewItem.value(), "", None, None)
    val newItemView = ManiMenuItemView(newItem)

    model.items = model.items :+ newItem
    itemsView = itemsView :+ newItemView
    createNavigation(newItemView)
    newItemView.addTo(sideMdenu.ref)
  })
  makeNewSubItem.confirm.onClick(_ => {
    implicit val siteModelView: SiteModelView = this
    val newItem = MenuItem(0, makeNewSubItem.value(), "", None, Some(currentItem.menuItem))
    val newItemView = SubMenuItemView(newItem)

    createNavigation(newItemView)
    currentItem.menuItem.items = currentItem.menuItem.items :+ newItem
    newItemView.addTo(mainContent.ref)
  })

  override def xml(): Node = <div>
    <div id="side-menu"></div>
    <div id="addMainMenu"></div>
    <div id="current-view"></div>
    <div id="addSubMenu"></div>
    <div id="choseItem"></div>
  </div>

  override def id: String = "sm-view"

  var currentItem: MenuItemView = _

  val choseItem: Ref[Div] = Ref("choseItem")

  def updateMainContent(i: MenuItem): Any = {

    if (i.items.nonEmpty) {
      var fresh: List[SubMenuItemView] = Nil
      mainContent.ref.innerHTML = i.items.map(SubMenuItemView.apply).map(iHtml => {
        fresh = iHtml :: fresh
        iHtml.onClick((_: Event) => {
          mainContent.ref.innerHTML = iHtml.menuItem.oeuvres.map(OeuvreView).map(_.xml().mkString).mkString
        })
        iHtml
      }
      ).map(_.xml().mkString).mkString
      fresh.foreach(_.updateView())
    } else if (i.oeuvres.nonEmpty) {
      mainContent.ref.innerHTML = i.oeuvres.map(OeuvreView).map(_.xml().mkString).mkString
    } else {
      mainContent.ref.innerHTML = ""
    }
  }

  def createNavigation(i: MenuItemView): MenuItemView = {

    i.onClick((_: Event) => {
      currentItem = i
      updateMainContent(i.menuItem)
    }
    )
    i
  }


  override def init(parent: HTMLElement): Unit = {

    super.init(parent)
    makeNewItem.addTo(addMainMenu.ref)
    makeNewSubItem.addTo(addSubMenu.ref)
    itemsView.map(createNavigation).foreach(e => e.addTo(sideMdenu.ref))


  }

  def contentChange(to: MenuItem): Unit = {
    updateMainContent(to)
  }

  def mainRemove(me: MenuItem): Unit = {
    itemsView.find(_.menuItem == me).foreach(_.removeFromView())
    itemsView = itemsView.filter(_.menuItem == me)
  }

  override def updateView(): Unit = {


  }
}

trait ValueConsumer[V] {
  def consume(v: V): Unit
}

class ChoooseMenuItem(valueConsumer: ValueConsumer[MenuItem])(implicit val siteService: SiteService, val siteModelView: SiteModelView) extends ParentComponent[Div] with ValueView[MenuItem] {


  private var _value: MenuItem = _

  override def value(): MenuItem = _value

  override def init(parent: HTMLElement): Unit = {
    listens.foreach(e => {
      e.ref.addEventListener("click", (ee: Event) => {
        _value = e.view.menuItem
        valueConsumer.consume(_value)
      })
    })
  }

  private var listens = List[RefComp[Div, MenuItemView]]()

  override def xml(): Node = <div id={id}>
    {siteService.siteModel.items.map(ManiMenuItemView.apply).map(e => {
      listens = Ref[Div, MenuItemView](e) :: listens
      val child = e.menuItem.items.map(ee => {
        val v = SubMenuItemView(ee)
        listens = Ref[Div, MenuItemView](v) :: listens
        v
      }).map(_.xml())
      <div>
        <div class="btn" id={e.id}>
          {e.xml()}
        </div>
        <div>
          {Group(child)}
        </div>
      </div>
    }
    )}
  </div>

  override def id: String = "choose"


}