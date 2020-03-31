package bon.jo.app

object ConfParam{
  val port = 80
  def apiMenu(): String = if(!JuliaConf.prod) s"http://localhost:$port/api/menu" else "/api/menu"
  def apiImage(): String = if(!JuliaConf.prod) s"http://localhost:$port/api/image"else "/api/image"
  def apiOeuvre(): String = if(!JuliaConf.prod) s"http://localhost:$port/api/oeuvre"else "/api/oeuvre"
}
