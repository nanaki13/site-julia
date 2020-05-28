package bon.jo.view

import bon.jo.SiteModel.{BaseMenuItem, Text, TextId}
import bon.jo.app.User
import bon.jo.html.DomShell.{$, $c, ExtendedElement, ExtendedHTMLCollection}
import bon.jo.html.XmlHtmlView
import bon.jo.phy.{Obs, ObsFact}
import bon.jo.service.Raws.TextExport
import bon.jo.service.{SiteService, TextService}
import org.scalajs.dom.html.{Div, TextArea}
import org.scalajs.dom.raw.HTMLElement
import bon.jo.phy.EventContext._

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Node

object APropos {

  case class AProposItemView(override val menuItem: ContactMenuItem
                             , override val id: String = "APropos-item"
                             , override val cssClass: String = "menu-item")(implicit siteService: SiteService)
    extends MenuItemView[ContactMenuItem](menuItem) {


    override def init(parent: HTMLElement): Unit = {

    }

    override def xml(): Node = commonXml(None)
  }

  case class ContactMenuItem(override val id: Int, override val text: String, override val link: String) extends BaseMenuItem(id, text, link)

  def getMenuItem: ContactMenuItem = {
    ContactMenuItem(-1, "A propos", "")
  }
}
trait PartAbs[+D]{def title: D;

  def paragraphs: List[D]
  def to[A](implicit el : PartAbs.Factory[D, A]) : PartAbs[A] = el(this)
}
  object PartAbs{
    trait Factory[-A, +C] extends Any {
      def apply(a : PartAbs[A]) : PartAbs[C]
    }
  }
case class PartExport(title: TextExport, paragraphs: List[TextExport]) extends PartAbs[TextExport]
case class Part(title: Text, paragraphs: List[Text]) extends PartAbs[Text]
class APropos(val textService: TextService)(implicit executionContext: ExecutionContext, user: User) extends XmlHtmlView[Div] {




  def tr(a: TextExport): Text = Text(TextId(a.id.uid,a.id.index),a.text)
  implicit object cv extends PartAbs.Factory[TextExport,Text]{
    override def apply(a: PartAbs[TextExport]): PartAbs[Text] = new Part(tr(a.title),a.paragraphs.map(tr) )
  }
  def addParts(tmp: List[PartAbs[Text]]): List[PartAbs[Text]] = {
    tmp foreach addPart
    tmp
  }


  val uidTitle = "APropos-title"
  def uidParts(index : Int): String = "APropos-para-" + index

  def readParts: Future[List[PartExport]] = {
    for {
      title <- textService.getByUid(uidTitle)
      ret <-  Future.sequence(  for {e <- title} yield {
        for {ee <- textService.getByUid(uidParts(e.id.index))} yield {
          PartExport(e, ee)
        }
      })
    } yield {
      ret
    }
  }


  override def xml(): Node = <div id={id}></div>

  override def id: String = "A propos"

  def addTiltePart(contactParagraph: Text): Div = {

    implicit val fct: () => Obs[String] = Obs.once[String]
    val elId =contactParagraph.id.toString + "-" + contactParagraph.id.index
    me.addChild(<div id ={elId+"-cont"} >
      <div class="title-para">
        <h2 id={elId} >{contactParagraph.text}</h2>
      </div></div>)
    if (user.role.admin) {
      $[Div](elId)
        .UserCanUpdate(inputView = Some((ui: String) => $c(<input  id={id + "edit" + contactParagraph.id}>
          {ui.trim}
        </input>).asInstanceOf[TextArea]), read = Some(
          (e: HTMLElement) => e.asInstanceOf[TextArea].value.trim
        )).suscribe(e => {
        textService.update(Text(TextId( contactParagraph.id.uid, contactParagraph.id.index), e))
      })
    }
    me.children.last.asInstanceOf[Div]
  }

  def addParagraph(contactParagraph : Text)(implicit parent : Div): Unit ={
    implicit val fct: () => Obs[String] = Obs.once[String]

    parent.addChild(<div id={contactParagraph.id.toString + "-" + contactParagraph.id.index} class="para">
      {contactParagraph.text}
    </div>)
    if (user.role.admin) {
      $[Div](contactParagraph.id.toString + "-" + contactParagraph.id.index)
        .UserCanUpdate(inputView = Some((ui: String) => $c(<textarea  cols="50" id={id + "edit" + contactParagraph.id}>
          {ui.trim}
        </textarea>).asInstanceOf[TextArea]), read = Some(
          (e: HTMLElement) => e.asInstanceOf[TextArea].value.trim
        )).suscribe(e => {
        textService.update(Text(TextId(contactParagraph.id.uid, contactParagraph.id.index), e))
      })
    }
  }
  def addPart(part: PartAbs[Text]): Unit = {

    val contactParagraph = part.title
    implicit val partContent: Div =  addTiltePart(contactParagraph)
    var maxIndexPatt: Int = (0 :: part.paragraphs.map(_.id.index)).max

    if (user.role.admin) {
      adder(uidParts(contactParagraph.id.index),() => {maxIndexPatt+=1;maxIndexPatt},partContent)
    }
    part.paragraphs.sortBy(_.id.index).foreach(addParagraph)


  }

  var maxIndexTitle = 0

  def adder(uid : String,index : ()=> Int,parent : Div) = {
    parent.classList.add("over-handle")
    val textAdd= if(uid == uidTitle){"Ajouter une partie"} else {"Ajouter une paragraph"}
    val btn =$c(<div class="hide-no-over" id={"addParaApropos"+uid}>{textAdd}</div>).asInstanceOf[Div]
    parent.appendChild(btn)
    btn.clkOnce.suscribe(e => {
      val ind = index()
      val toSave = Text(TextId( uid, ind), "Remplit moi")

      textService.save(toSave).foreach(s =>{
        if(uid == uidTitle){
          addPart(Part(toSave,Nil))
        }else{
          addParagraph(toSave)(parent)
        }
      })
    })
    btn
  }
  override def init(parent: HTMLElement): Unit = {
    val allText: Future[List[PartExport]] = readParts
    if (user.role.admin) {
      adder(uidTitle,() => {maxIndexTitle+=1;maxIndexTitle},me)
    }

    implicit val ct : Div = me
    allText.map(e => {

      maxIndexTitle = (maxIndexTitle :: e.map(_.title.id.index)).max
     val ret: List[PartAbs[Text]] = e.map(_.to[Text])
      ret.sortBy(_.title.id.index)
    }).foreach(addParts)


  }




}


