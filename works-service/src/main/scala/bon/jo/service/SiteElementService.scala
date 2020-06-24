package bon.jo.service

import bon.jo.juliasite.model.Schema.{Descri, SiteElement}
import bon.jo.{RawImpl, RootCreator, WebServiceCrud}

import scala.concurrent.Future


trait SiteElementService extends Service[RawImpl.SiteElementExport]
  with IntId[RawImpl.SiteElementExport]
  with WebServiceCrud[RawImpl.SiteElementExport,Int]
  with RootCreator[RawImpl.SiteElementExport,Int]{

}

