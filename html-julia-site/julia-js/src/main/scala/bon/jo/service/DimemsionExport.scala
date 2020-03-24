package bon.jo.service

import bon.jo.SiteModel.Dimension
import bon.jo.service.Raws.DimemsionExport

import scala.scalajs.js

object DimemsionExport {
  def apply(d: Dimension): DimemsionExport = js.Dynamic.literal(x = d.x, y = d.y).asInstanceOf[DimemsionExport]
}
