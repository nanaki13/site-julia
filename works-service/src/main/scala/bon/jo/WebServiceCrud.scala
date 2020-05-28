package bon.jo

import bon.jo.SiteModel.OkResponse
import bon.jo.service.Service

import scala.concurrent.Future

trait WebServiceCrud[WebMessage <: OkResponse,ID] {

  self: Service[WebMessage] =>

  def createEntity(m: WebMessage): Future[Option[WebMessage]]

  def readEntity(m: ID): Future[Option[WebMessage]]

  def readAll: Future[IterableOnce[WebMessage]]

  def deleteEntity(m: ID): Future[Boolean]

  def updateEntity(m: WebMessage): Future[Option[WebMessage]]

  def ressourceName: String
}
