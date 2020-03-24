package bon.jo.app

object ConfParam{
  def apiMenu(): String = if(!JuliaConf.prod) "http://localhost:8080/api/menu" else ""
  def apiImage(): String = if(!JuliaConf.prod) "http://localhost:8080/api/image"else ""
  def apiOeuvre(): String = if(!JuliaConf.prod) "http://localhost:8080/api/oeuvre"else ""
}
