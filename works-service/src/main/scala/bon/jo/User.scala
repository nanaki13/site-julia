package bon.jo.app

import bon.jo.js


@js.native
trait TokenPlayLoad extends js.Object {
  val name: String
  val role: String


}

object TokenPlayLoad {

  case class Impl(name: String, role: String) extends TokenPlayLoad

  val Visitor = Impl("Visitor","")

}

@js.native
trait Role extends js.Object {
  val admin: Boolean

}

object Role {
  val Visitor: Role = Impl(false)
  val Admin: Role = Impl(true)

  case class Impl(admin: Boolean) extends Role

}