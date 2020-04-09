package bon.jo

import org.json4s.{CustomSerializer, DefaultFormats, Formats}
import org.json4s.JsonAST.{JInt, JNothing, JNull}

object CustomJs extends CustomSerializer[js.BigInt](format =>
  ( {
    case JInt(s) => js.BigInt(s.toInt)
    case JNull => js.BigInt(0, true)
    case JNothing => js.BigInt(0, true)
  }, {

    case e: js.BigInt => if (e.isNull) JNothing else JInt(e.v)
  })) {

  implicit val formatsOut: Formats = DefaultFormats + LocalDateSerializer + CustomJs
}
