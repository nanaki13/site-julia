package bon.jo.view

import bon.jo.SiteModel.{BaseMenuItem, ThemeMenuItem}
import bon.jo.app.User
import bon.jo.html.DomShell.{$, ExtendedElement}
import bon.jo.html.Types.{FinalComponent, ParentComponent}
import bon.jo.html._
import bon.jo.service.SiteService
import bon.jo.view.APropos.ContactMenuItem
import bon.jo.{Logger, SiteModel}
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, PopStateEvent}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.Date
import scala.xml.Node


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

  implicit val user: User = siteService.user

  implicit val siteModelView: SiteModelView = this
  // val mainContent: Ref[Div] = Ref("current-view")
  val sideMdenu: Ref[Div] = Ref("side-menu")
  val addSubMenu: Ref[Div] = Ref("addSubMenu")
  val addMainMenu: Ref[Div] = Ref("addMainMenu")
  val pageTitle: Ref[Div] = Ref("page-title")
  lazy val rootView: Div = $[Div]("sm-view")

  def baseItem: List[MenuItemView[_ <: BaseMenuItem]] = APropos.AProposItemView(APropos.getMenuItem) :: model.items.map(ManiMenuItemView.apply)

  var itemsView: List[MenuItemView[_ <: BaseMenuItem]] = baseItem


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
    siteService.menuService.save(newItem).foreach(e => {
      val newItemView = ManiMenuItemView(newItem)
      model.items = model.items :+ newItem
      itemsView = itemsView :+ newItemView

      newItemView.addTo(sideMdenu.ref)
      createNavigation(newItemView)
    })

  })

  def createConfirmSubMenuAdd(makeNewItem: SimpleInput): Unit = makeNewItem.confirm.obs.suscribe(_ => {
    implicit val siteModelView: SiteModelView = this
    val newItem = siteService.createNewSubMenuItem(makeNewItem.value(), currentItem.menuItem.asInstanceOf[ThemeMenuItem])
    siteService.menuService.save(newItem).foreach(e => {
      val newItemView = SubMenuItemView(newItem)
      val t = currentItem.asInstanceOf[MenuItemView[ThemeMenuItem]]
      t.menuItem.items = t.menuItem.items :+ newItem
      newContent.itemList.contentRef.ref.addChild(newItemView.xml())
      newItemView.init(newContent.itemList.contentRef.ref)
      createNavigation(newItemView)
    })

  })

  override def xml(): Node = <div id="sm-view">
    <div id="side-menu">
      <div id="addMainMenu" class="simple-input"></div>
    </div>
    <div id="current-view">
      <div id="page-title"></div>
      <div id="addSubMenu" class="simple-input"></div>
    </div>
  </div>

  override def id: String = "sm-view"

  var _currentItem: MenuItemView[_ <: BaseMenuItem] = _

  def currentItem: MenuItemView[_ <: BaseMenuItem] = _currentItem

  def currentItem_=(z: MenuItemView[_ <: BaseMenuItem]): Unit = {
    _currentItem = z
  }

  val choseItem: Ref[Div] = Ref("choseItem")


  case class MainContent() extends ParentComponent[Div] {
    val eouvreList: SimpleList[OeuvreView] = Lists.PagList[OeuvreView](idp = "lo", cssClassp = "column-display", addElementp = Some(() => {
      val newOevure: SiteModel.Oeuvre = SiteModel.Oeuvre(siteService.oeuvreService.newId).copy(theme = Some(currentItem.menuItem.asInstanceOf[ThemeMenuItem]),date = (new Date()).getFullYear().toInt)
      siteService.oeuvreService
        .save(newOevure).map(_ => OeuvreView(newOevure))
    }), deletep = e => {
      e.notInDom()
    },_.copy())
    var itemList: SimpleList[SubMenuItemView] = SimpleList[SubMenuItemView]("li", "container-fluid")

    lazy val custom: Div = $[Div](id + "-c")
    add(itemList)
    add(eouvreList)


    override def init(parent: HTMLElement): Unit = {
      me.appendChild(itemList.html())
      me.appendChild(eouvreList.html())
      super.init(parent)


      org.scalajs.dom.window.addEventListener[PopStateEvent]("popstate", { e => {
        val i = currentItem.menuItem


        val path = s"/site/${i.id}/" + i.text.replaceAll("\\s+", "-")

        //     org.scalajs.dom.window.history.replaceState(i.id.toString, i.text, path)
        itemsView.find(_.menuItem.id == e.state.toString.toInt).map(load(_, fromHistory = true)).orElse({
          model.allItem.find(_.id == e.state.toString.toInt).map(e => {
            if (e.parent.isDefined) {
              load(SubMenuItemView(e), fromHistory = true)
            }
          })
        })

      }
      })
    }

    def clearAllContent(): Unit = {
      eouvreList.clear()
      itemList.clear()
      custom.clear()
    }

    def load(contactMenuItem: ContactMenuItem): Unit = {

      clearAllContent()
      val c = new APropos(siteService.textService)
      custom.addChild(c.xml())
      c.init(custom)

    }

    def load(t: ThemeMenuItem): Unit = {
      clearAllContent()
      eouvreList.clearAndAddAll(t.oeuvres.map(OeuvreView.apply))

      val v = t.items.map(SubMenuItemView.apply)
      itemList.clearAndAddAll(v).foreach(ii => {
        ii.link.ref.clkOnce().suscribe(e => {

          load(ii)
        })
      })
    }

    def load(ii: MenuItemView[_ <: BaseMenuItem], fromHistory: Boolean = false): Unit = {
      currentItem = ii
      pageTitle.ref.innerText = ii.menuItem.text
      org.scalajs.dom.document.title = ii.menuItem.text
      val i = ii.menuItem
      val path = s"/site/${i.id}/" + i.text.replaceAll("\\s+", "-")

      if (!fromHistory) {
        org.scalajs.dom.window.history.pushState(i.id.toString, org.scalajs.dom.document.title, path)
      }

      i match {
        case c: ContactMenuItem => load(c)
        case t: ThemeMenuItem => load(t)
        case _ =>
      }


    }


    override def xml(): Node

    = <div id={id}>
      <div id={id + "-c"}></div>
    </div>


    override def id: String

    = "main-content"


  }


  def updateMainContent(i: MenuItemView[_ <: BaseMenuItem]): Any = {

    newContent.load(i)
  }

  def createNavigation(i: MenuItemView[_ <: BaseMenuItem]): MenuItemView[_ <: BaseMenuItem] = {

    i.link.ref.clkOnce().suscribe((_: Event) => {
      if (currentItem == null) {
        makeNewSubItem.foreach(_.addTo(addSubMenu.ref))
      }

      updateMainContent(i)
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
    val title : SiteTitle = SiteTitle(siteService.textService)
    title.addTo(sideMdenu.ref)
    itemsView.map(e => {
      e.addTo(sideMdenu.ref);
      e
    }).foreach(createNavigation)


  }

  def contentChange(to: ThemeMenuItem): Unit = {
    itemsView.find(_.menuItem == to) match {
      case Some(value) => updateMainContent(value)
      case None => Logger.log("Cant find " + to.id)
    }

  }

  def mainRemove(me: ThemeMenuItem): Unit = {
    itemsView.find(_.menuItem == me).foreach(_.removeFromView())
    itemsView = itemsView.filter(_.menuItem == me)
  }

  def modelChange(): Any = {

    itemsView.foreach(_.removeFromView())

    itemsView = baseItem
    itemsView.map(e => {
      e.addTo(sideMdenu.ref);
      e
    }).foreach(createNavigation)
  }


}







