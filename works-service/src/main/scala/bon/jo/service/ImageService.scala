package bon.jo.service

import bon.jo.{RawImpl, RootCreator, WebServiceCrud}

trait ImageService extends Service[RawImpl.ImageRawExport] with IntId[RawImpl.ImageRawExport] with WebServiceCrud[RawImpl.ImageRawExport,Int] with RootCreator[RawImpl.ImageRawExport,Int]
