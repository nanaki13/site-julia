package bon.jo.view

import bon.jo.SiteModel
import bon.jo.SiteModel.MenuItem
import bon.jo.html.DomShell.ExtendedElement
import bon.jo.html.Types.{FinalComponent, ParentComponent}
import bon.jo.html._
import bon.jo.service.SiteService
import org.scalajs.dom.Event
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.HTMLElement

import scala.xml.{Group, Node}


case class SiteModelView(model: SiteModel)(implicit val siteService: SiteService) extends XmlHtmlView[Div] with InDom[Div] with NodeView[Div] {


  val admin = true

  implicit val siteModelView: SiteModelView = this
  // val mainContent: Ref[Div] = Ref("current-view")
  val sideMdenu: Ref[Div] = Ref("side-menu")
  val addSubMenu: Ref[Div] = Ref("addSubMenu")
  val addMainMenu: Ref[Div] = Ref("addMainMenu")


  var itemsView: List[ManiMenuItemView] = model.items.map(ManiMenuItemView.apply)

  val makeNewItem: SimpleInput = SimpleInput("newItem", "nom du menu",title = Some("Ajouter un menu"))
  val makeNewSubItem: SimpleInput = SimpleInput("newSubItem", "nom du menu",title = Some("Ajouter un sous menu"))


  val newContent: MainContent = MainContent()
  val newContentRef: Ref[Div] = Ref("current-view")
  makeNewItem.confirm.onClick(_ => {
    val newItem = siteService.createNewMainMenuItem(makeNewItem.value())
    val newItemView = ManiMenuItemView(newItem)

    model.items = model.items :+ newItem
    itemsView = itemsView :+ newItemView
    createNavigation(newItemView)
    newItemView.addTo(sideMdenu.ref)
  })
  makeNewSubItem.confirm.onClick(_ => {
    implicit val siteModelView: SiteModelView = this
    val newItem = siteService.createNewSubMenuItem(makeNewSubItem.value(), currentItem.menuItem)
    val newItemView = SubMenuItemView(newItem)

    createNavigation(newItemView)
    currentItem.menuItem.items = currentItem.menuItem.items :+ newItem
    newItemView.addTo(newContent.itemList.me)
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
    var eouvreList: SimpleList[OeuvreView] = SimpleList[OeuvreView]("lo")
    var itemList: SimpleList[SubMenuItemView] = SimpleList[SubMenuItemView]("li")

    add(itemList)
    add(eouvreList)

    def load(i: MenuItem): Unit = {
      println("load item in main content : " + i.id)
      val path = (i.parent match {
        case Some(value) => value.text.replaceAll("\\s+", "-") + "/"
        case None => "/"
      }) + i.text.replaceAll("\\s+", "-")

      org.scalajs.dom.window.history.pushState("", i.text, path)

      println("load item in main content : " + i.oeuvres.groupMapReduce[Int, Int](e => e.id)(_ => 1)(_ + _))
      eouvreList.clearAndAddAll(i.oeuvres.map(OeuvreView.apply))
      println("load item in main content : " + i.id)
      val v = i.items.map(SubMenuItemView.apply)
      v.foreach(ii => {
        ii.link.onClick(e => {
          load(ii.menuItem)
        })
      })
      itemList.clearAndAddAll(v)

    }


    override def xml(): Node = <div id={id}></div>


    override def id: String = "main-content"


  }


  def updateMainContent(i: MenuItem): Any = {
    newContent.load(i)
  }

  def createNavigation(i: MenuItemView): MenuItemView = {

    i.link.onClick((_: Event) => {
      if (currentItem == null) {
        makeNewSubItem.addTo(addSubMenu.ref)
      }
      currentItem = i
      updateMainContent(i.menuItem)
    }
    )
    i
  }


  override def init(parent: HTMLElement): Unit = {

    super.init(parent)
    newContent.addTo(newContentRef.ref)
    makeNewItem.addTo(addMainMenu.ref)

    itemsView.map(createNavigation).foreach(e => e.addTo(sideMdenu.ref))

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
    itemsView.map(createNavigation).foreach(e => e.addTo(sideMdenu.ref))
  }

  override def updateView(): Unit = {


  }
}


class ChoooseMenuItem(valueConsumer: ValueConsumer[MenuItem])
                     (implicit val siteService: SiteService)
  extends ParentComponent[Div] with ValueView[MenuItem] {


  private var _value: MenuItem = _

  override def value(): MenuItem = _value

  override def init(parent: HTMLElement): Unit = {
    listens.added.foreach(e => {
      e.me.addEventListener("click", (ee: Event) => {
        _value = e.menuItem
        valueConsumer.consume(_value)
      })
    })
  }

  private var listens = SimleTree[IdMenuItemVew]("c-li", siteService.siteModel.items.map(i => IdMenuItemVew(id + "-" + i.id, i)),
    e => {
      e.menuItem.items.map(i => IdMenuItemVew(id + "-" + i.id, i))
    }
  )

  override def xml(): Node = <div id={id}>
    {listens.xml()}
  </div>


  override def id: String = "choose"


}

case class SimpleList[Finalp <: FinalComponent[_]](override val id: String) extends FinalComponent[Div] {
  override def xml(): Node = <div id={id}></div>


  def clearAndAddAll(cps: List[Finalp]): Unit = {
    me.clear();
    DomShell.deb()
    //  cps.foreach(e => me.appendChild(e.html().asInstanceOf[HTMLElement]));
    DomShell.deb()
    cps.foreach(_.addTo(me))
    DomShell.deb()
    // init(parent)
  }

  override def init(parentp: HTMLElement): Unit = {
    parentp.appendChild(html());

  }
}

case class SimleTree[Finalp <: FinalComponent[_]](override val id: String, cps: List[Finalp], children: Finalp => List[Finalp]) extends ParentComponent[Div] {

  var added: List[Finalp] = Nil

  def register(finalp: Finalp) = {
    added = added :+ finalp
  }

  def xml(e: Finalp): Node = {
    register(e)
    <div>
      {e.xml}{val c = children(e)
    if (c.nonEmpty) {
      <div>
        {Group(c.map(e => xml(e)))}
      </div>
    } else {
      <div></div>
    }}
    </div>
  }

  override def xml(): Node = <div id={id}>
    {if (cps.nonEmpty) {
      cps.map(xml)
    }}
  </div>
}
