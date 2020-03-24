package bon.jo

import bon.jo.SiteModel.OkResponse

import scala.concurrent.Future

trait WebServiceCrud[WebMessage <: OkResponse] {

  self: Service[WebMessage] =>

  def createEntity(m: WebMessage): Future[Option[WebMessage]]

  def readEntity(m: Int): Future[Option[WebMessage]]

  def readAll: Future[IterableOnce[WebMessage]]

  def deleteEntity(m: Int): Future[Boolean]

  def updateEntity(m: WebMessage): Future[Option[WebMessage]]

  def ressourceName: String
}
