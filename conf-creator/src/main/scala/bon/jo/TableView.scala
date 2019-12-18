package bon.jo

trait TableView {
  def headers: Seq[String]

  def data: Array[Seq[AnyRef]]

  def headersToIndex: Map[String, Int] = {
    headers.zipWithIndex.toMap
  }

  private val _headersToIndex: Map[String, Int] = headersToIndex

  def getIndex(header: String): Int = _headersToIndex(header)

  def getValue(row : Int,header : String): AnyRef = data(row)(getIndex(header))



  def get[T](row : Int)(implicit tr : Seq[AnyRef] => T): T =   tr(data(row))
}

case class InMemmoryTable(override val data: Array[Seq[AnyRef]],override val headers: Seq[String] ) extends TableView{
  override def headers: Seq[String] = Seq("name","first_name")

}
case class User(name:String,fn:String)
object Main extends App{
 val data: Array[Seq[AnyRef]] = Array(
    Seq("Bobb","bill"),
    Seq("roger","andrer"),
  )
  val table = InMemmoryTable(data, Seq("name","first_name"))
  implicit def tr(p : Seq[AnyRef]) : User = {
    var e = User.curried
    (for(v <- p )yield e(v.toString) )

  }

  println( table.get[User](0))

}
