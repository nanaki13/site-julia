package bon.jo.view

import bon.jo.SiteModel.MenuItem
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.{FinalComponent, ParentComponent}
import bon.jo.html._
import bon.jo.service.SiteService
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Group, Node}


case class SiteModelView(model: SiteModel)(implicit val siteService: SiteService, executionContext: ExecutionContext) extends XmlHtmlView[Div] with InDom[Div] with NodeView[Div] {
  def root(listImg: FinalComponent[_]) = {
    val praent = rootView.parentNode.asInstanceOf[HTMLElement]
    listImg.addTo(praent)
  }

  var dis: String = _

  def hideAll = {
    dis = rootView.style.display
    rootView.style.display = "none"
  }

  def displayAll = {
    rootView.style.display = dis
  }


  implicit val siteModelView: SiteModelView = this
  // val mainContent: Ref[Div] = Ref("current-view")
  val sideMdenu: Ref[Div] = Ref("side-menu")
  val addSubMenu: Ref[Div] = Ref("addSubMenu")
  val addMainMenu: Ref[Div] = Ref("addMainMenu")

  lazy val rootView: Div = $[Div]("sm-view")

  var itemsView: List[ManiMenuItemView] = model.items.map(ManiMenuItemView.apply)


  val newContent: MainContent = MainContent()
  val newContentRef: Ref[Div] = Ref("current-view")


  case class MenusAdd(menu: SimpleInput, subMenu: SimpleInput)

  def createMenuAdd: Option[MenusAdd] = if (siteService.user.role.admin) {
    val makeNewItem: SimpleInput = SimpleInput("newItem", "nom du menu", title = Some("Ajouter un menu"))
    val makeNewSubItem: SimpleInput = SimpleInput("newSubItem", "nom du menu", title = Some("Ajouter un sous menu"))
    createConfirmMenuAdd(makeNewItem)
    createConfirmSubMenuAdd(makeNewSubItem)
    Some(MenusAdd(makeNewItem, makeNewSubItem))
  } else {
    None
  }

  def createConfirmMenuAdd(makeNewItem: SimpleInput): Unit = makeNewItem.confirm.obs.suscribe(_ => {
    val newItem = siteService.createNewMainMenuItem(makeNewItem.value())
    val newItemView = ManiMenuItemView(newItem)
    model.items = model.items :+ newItem
    itemsView = itemsView :+ newItemView
    newItemView.addTo(sideMdenu.ref)
    createNavigation(newItemView)
  })

  def createConfirmSubMenuAdd(makeNewItem: SimpleInput): Unit = makeNewItem.confirm.obs.suscribe(_ => {
    implicit val siteModelView: SiteModelView = this
    val newItem = siteService.createNewSubMenuItem(makeNewItem.value(), currentItem.menuItem)
    val newItemView = SubMenuItemView(newItem)
    currentItem.menuItem.items = currentItem.menuItem.items :+ newItem
    newItemView.addTo(newContent.itemList.me)
    createNavigation(newItemView)
  })

  override def xml(): Node = <div id="sm-view">
    <div id="side-menu">
      <div id="addMainMenu" class="simple-input"></div>
    </div>
    <div id="current-view">
      <div id="addSubMenu" class="simple-input"></div>
    </div>
  </div>

  override def id: String = "sm-view"

  var currentItem: MenuItemView = _

  val choseItem: Ref[Div] = Ref("choseItem")


  case class MainContent() extends ParentComponent[Div] {
    var eouvreList: SimpleList[OeuvreView] = Lists.PagList[OeuvreView](idp="lo",cssClassp="column-display",e=>{e.notInDom})
    var itemList: SimpleList[SubMenuItemView] = SimpleList[SubMenuItemView]("li","container-fluid")

    add(itemList)
    add(eouvreList)


    override def init(parent: HTMLElement): Unit = {
      me.appendChild(itemList.html())
      me.appendChild(eouvreList.html())
      super.init(parent)


    }

    def load(i: MenuItem): Unit = {

      val path = (i.parent match {
        case Some(value) => value.text.replaceAll("\\s+", "-") + "/"
        case None => "/"
      }) + i.text.replaceAll("\\s+", "-")

      org.scalajs.dom.window.history.pushState("", i.text, path)
      eouvreList.clearAndAddAll(i.oeuvres.map(OeuvreView.apply))

      val v = i.items.map(SubMenuItemView.apply)
      itemList.clearAndAddAll(v).foreach(ii => {
        ii.link.ref.clkOnce().suscribe(e=>{
          load(ii.menuItem)
        })
      })



    }


    override def xml(): Node = <div id={id}></div>


    override def id: String = "main-content"


  }


  def updateMainContent(i: MenuItem): Any = {
    newContent.load(i)
  }

  def createNavigation(i: MenuItemView): MenuItemView = {

    i.link.ref.clkOnce().suscribe((_: Event) => {
      if (currentItem == null) {
        makeNewSubItem.foreach(_.addTo(addSubMenu.ref))
      }
      currentItem = i
      updateMainContent(i.menuItem)
    }
    )
    i
  }

  var makeNewSubItem: Option[SimpleInput] = _

  override def init(parent: HTMLElement): Unit = {
    val menusInput = createMenuAdd
    makeNewSubItem = menusInput.map(_.subMenu)
    super.init(parent)
    newContent.addTo(newContentRef.ref)
    menusInput.foreach(e => e.menu.addTo(addMainMenu.ref))

    itemsView.map(e => {e.addTo(sideMdenu.ref);e}).foreach(createNavigation)


  }

  def contentChange(to: MenuItem): Unit = {
    updateMainContent(to)
  }

  def mainRemove(me: MenuItem): Unit = {
    itemsView.find(_.menuItem == me).foreach(_.removeFromView())
    itemsView = itemsView.filter(_.menuItem == me)
  }

  def modelChange(): Any = {

    itemsView.foreach(_.removeFromView())

    DomShell.deb()
    itemsView = model.items.map(ManiMenuItemView.apply)
    itemsView.map(e => {e.addTo(sideMdenu.ref);e}).foreach(createNavigation)
  }


}







