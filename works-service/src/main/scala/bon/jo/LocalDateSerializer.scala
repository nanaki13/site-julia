package bon.jo

import java.time.LocalDate

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

case object LocalDateSerializer
  extends CustomSerializer[LocalDate](
    format =>
      ({
        case JString(s) => LocalDate.parse(s.substring(0,10))
      }, {
        case e : LocalDate => JString(e.toString)
      })
  )
