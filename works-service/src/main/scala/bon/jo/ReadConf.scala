package bon.jo
import com.typesafe.config.{Config, ConfigFactory}
object ReadConf {


  val   conf : ConfReaded = read()
  case class ConfReaded(baseApiUrl : String) {
    val baseApiUrlImage: String = baseApiUrl+"/image"

  }

  def read(): ConfReaded ={
    ConfReaded(ConfigFactory.load().getString("site.api-base-url"))
  }
}
