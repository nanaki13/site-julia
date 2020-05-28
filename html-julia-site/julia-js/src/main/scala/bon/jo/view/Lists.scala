package bon.jo.view

import bon.jo.Logger
import bon.jo.html.DomShell.ExtendedElement
import bon.jo.html.Types.FinalComponent
import bon.jo.phy.Obs
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

import scala.concurrent.{ExecutionContext, Future}

object Lists {

  object PagList {
    def apply[Finalp <: FinalComponent[_ <: HTMLElement]](
                                                           idp: String,
                                                           cssClassp: String ,
                                                           addElementp: Option[() => Future[Finalp]],
                                                           deletep : Finalp=>Unit,
                                                           rebuildp : Finalp=>Finalp )
                                                         (implicit executionContext: ExecutionContext): PagList[Finalp] =
      new PaginableList[Finalp] {
      override implicit val ex: ExecutionContext =executionContext
      override def cssClass: String = cssClassp

      override def id: String = idp

      override def addedInView(el: Finalp): Unit = {}
      override def deletedInView(e : Finalp): Unit = deletep(e)
      override var addElement: Option[() => Future[Finalp]] = addElementp

        override def rebuild(finalp: Finalp): Finalp = rebuildp(finalp)
      }
  }

  object PagChooseList {
    def apply[A, Finalp <: FinalComponent[_ <: HTMLElement]](
                                                              idp: String,
                                                              cssClassp: String,
                                                               mappingp: Finalp => A,
                                                              rebuildp : Finalp=>Finalp,
                                                              addElementp:  Option[() => Future[Finalp]],
                                                              deletep : Finalp=>Unit = (e : Finalp )=> {}

                                                            )(implicit executionContext: ExecutionContext): PagChooseList[A, Finalp] = new PaginableList[Finalp] with ChooseList[A, Finalp] {
      override implicit val ex: ExecutionContext =executionContext
      override def cssClass: String = cssClassp

      override def id: String = idp

      override def mapping(p: Finalp): A = mappingp(p)
      override def deletedInView(e : Finalp): Unit = deletep(e)

      override var addElement: Option[() => Future[Finalp]] = addElementp

      override def rebuild(finalp: Finalp): Finalp = rebuildp(finalp)
    }
  }

  type PagList[Finalp <: FinalComponent[_ <: HTMLElement]] = SimpleList[Finalp] with PaginableList[Finalp]

  type PagChooseList[A, Finalp <: FinalComponent[_ <: HTMLElement]] = SimpleList[Finalp] with PaginableList[Finalp] with ChooseList[A, Finalp]

  trait ChooseList[A, Finalp <: FinalComponent[_ <: HTMLElement]] extends Obs[A] with SimpleList[Finalp] with PaginableList[Finalp] {


    override def addedInView(e: Finalp): Unit = {
      Logger.log(""+e.id)
      e.me.clkOnce().suscribe((ev: MouseEvent) => obs.newValue(mapping(e)))
    }

    override def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
      val fromSup = super.clearAndAddAll(cps)
      fromSup.filter(_.isInDom) foreach addedInView
      fromSup
    }

    override def suscribe(client: A => Unit): Unit = obs.suscribe(client)

    override def newValue(a: A): Unit = obs.newValue(a)


    override def clearClients = obs.clearClients

    def mapping(p: Finalp): A


    val obs: Obs[A] = Obs.get[A](id + "obs-gen")

  }

}

