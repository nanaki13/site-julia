package bon.jo.view

import bon.jo.html.DomShell.{$, ExtendedElement, Obs, OnceObs}
import bon.jo.html.Types.FinalComponent
import org.scalajs.dom.html.{Div, Span}
import org.scalajs.dom.raw.{HTMLElement, MouseEvent}

import scala.scalajs.js
import scala.xml.Node

object Lists {

  object PagList {
    def apply[Finalp <: FinalComponent[_ <: HTMLElement]](
                                                           idp: String,
                                                           cssClassp: String
                                                        ,deletep : Finalp=>Unit ): PagList[Finalp] = new SimpleList[Finalp] with PaginableList[Finalp] {
      override def cssClass: String = cssClassp

      override def id: String = idp

      override def addedInView(el: Finalp): Unit = {}
      override def deletedInView(e : Finalp): Unit = deletep(e)
    }
  }

  object PagChooseList {
    def apply[A, Finalp <: FinalComponent[_ <: HTMLElement]](
                                                              idp: String,
                                                              cssClassp: String
                                                              , mappingp: Finalp => A

                                                              ,deletep : Finalp=>Unit = (e : Finalp )=> {} ): PagChooseList[A, Finalp] = new PaginableList[Finalp] with ChooseList[A, Finalp] {
      override def cssClass: String = cssClassp

      override def id: String = idp

      override def mapping(p: Finalp): A = mappingp(p)
      override def deletedInView(e : Finalp): Unit = deletep(e)
    }
  }

  type PagList[Finalp <: FinalComponent[_ <: HTMLElement]] = SimpleList[Finalp] with PaginableList[Finalp]

  type PagChooseList[A, Finalp <: FinalComponent[_ <: HTMLElement]] = SimpleList[Finalp] with PaginableList[Finalp] with ChooseList[A, Finalp]

  trait ChooseList[A, Finalp <: FinalComponent[_ <: HTMLElement]] extends Obs[A] with SimpleList[Finalp] with PaginableList[Finalp] {


    override def addedInView(e: Finalp): Unit = {
      e.me.clkOnce().suscribe((ev: MouseEvent) => obs.newValue(mapping(e)))
    }

    override def clearAndAddAll(cps: List[Finalp]): List[Finalp] = {
      val fromSup = super.clearAndAddAll(cps)
      fromSup foreach addedInView
      fromSup
    }

    override def suscribe(client: A => Unit): Unit = obs.suscribe(client)

    override def newValue(a: A): Unit = obs.newValue(a)


    override def clearClients = obs.clearClients

    def mapping(p: Finalp): A


    val obs: Obs[A] = Obs.get[A](id + "obs-gen")

  }

}

