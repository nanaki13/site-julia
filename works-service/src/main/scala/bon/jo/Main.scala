package bon.jo

object Main {
  def main(args: Array[String]): Unit = {
    val port: Int = sys.env.getOrElse("PORT", "80").toInt
    WebServer.startServer("0.0.0.0", port)
  }
}
