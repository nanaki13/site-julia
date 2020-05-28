package bon.jo.service

import bon.jo.{RawImpl, RootCreator, WebServiceCrud}

trait OeuvreService extends Service[RawImpl.OeuvreRawExport] with IntId[RawImpl.OeuvreRawExport] with WebServiceCrud[RawImpl.OeuvreRawExport,Int] with RootCreator[RawImpl.OeuvreRawExport,Int]
