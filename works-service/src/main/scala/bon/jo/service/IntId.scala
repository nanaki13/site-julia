package bon.jo.service

import bon.jo.{RootCreator, WebServiceCrud}
import bon.jo.SiteModel.OkResponse

trait IntId[A <: OkResponse] extends Service[A] with WebServiceCrud[A,Int] with RootCreator[A,Int]{
  override def stringToId(id: List[String]): Int = id.head.toInt
}
