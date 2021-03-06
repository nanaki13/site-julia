package bon.jo

import bon.jo.Raws.TextId
import bon.jo.SiteModel.OkResponse

object Raws {

  @js.native
  trait WithId extends js.Object {
    val id: Int
  }

  @js.native
  trait OeuvreRaw extends js.Object {
    val date: String
    val description: String
    val dimension: String
    val enable: String
    val id: String
    val image_key: String
    val tech_code: String
    val theme_key: String
    val title: String
  }

  @js.native
  trait ThemeRaw extends js.Object {
    val id: String
    val name: String
  }


  @js.native
  trait ImageRaw extends js.Object {
    val height: String
    val image_key: String
    val path: String
    val width: String

  }


  @js.native
  trait ImageRawExport extends js.Object {
    val id: Int
    val link: String
    val base: String
  }

  @js.native
  trait TextId extends js.Object {
    val uid: String
    val index: Int
  }

  @js.native
  trait TextExport extends js.Object {
    val id: TextId
    //val id: Int
    val text: String
  }

  @js.native
  trait SiteElementExport extends js.Object {
    val id: Int
    val imageId: js.BigInt
    val desc: Int
    val order: Int
  }

  @js.native
  trait ItemRawExport extends js.Object {
    val id: Int
    val text: String
    val link: String
    val image: js.BigInt
    val parent: js.BigInt
    val x: js.BigInt
    val y: js.BigInt
    val children: js.Array[Int]
    val oeuvres: js.Array[Int]
  }

  @js.native
  trait DimemsionExport extends js.Object {
    val x: Float
    val y: Float
  }

  @js.native
  trait OeuvreRawExport extends js.Object {
    val id: Int
    val image: js.BigInt
    val name: String
    val dimension: DimemsionExport
    val date: Int
    val theme: Int
    val description: String
  }

  @js.native
  trait GlobalExport extends js.Object {
    val items: js.Array[ItemRawExport] = js.native
    val oeuvres: js.Array[OeuvreRawExport] = js.native
    val images: js.Array[ImageRawExport] = js.native
  }


}

object RawImpl {

  case class SiteElementExport(id: Int,
                               imageId: js.BigInt,
                               desc: Int,
                               order: Int) extends Raws.SiteElementExport

  case class TextId(uid: String, index: Int) extends Raws.TextId

  case class TextExport(
                         //id: Int,
                         id: TextId,
                         text: String) extends Raws.TextExport

  case class ItemRawExport(id: Int,
                           text: String,
                           link: String,
                           image: js.BigInt,
                           parent: js.BigInt,
                           x: js.BigInt,
                           y: js.BigInt,
                           children: js.Array[Int] = Nil,
                           oeuvres: js.Array[Int] = Nil) extends Raws.ItemRawExport {

  }

  object ItemRawExport {
    def apply(id: Int,
              text: String,
              link: String,
              image: Int,
              parent: Int,
              x: Int,
              y: Int): ItemRawExport = ItemRawExport(id,
      text,
      link,
      js.BigInt(image),
      js.BigInt(parent),
      js.BigInt(x),
      js.BigInt(y))
  }

  case class ImageRawExport(id: Int, link: String, base: String) extends Raws.ImageRawExport

  case class OeuvreRawExport(
                              id: Int,
                              image: js.BigInt,
                              name: String,
                              dimension: DimemsionExport,
                              date: Int,
                              theme: Int,
                              description: String
                            ) extends Raws.OeuvreRawExport {


  }

  case class DimemsionExport(x: Float, y: Float) extends Raws.DimemsionExport

}

object js extends jsInterface {
  override def native[R]: R = {
    null.asInstanceOf[R]
  }

  object BigInt {
    def Null: BigInt = BigInt(0, true)

    def apply(option: Option[Int]): BigInt = {
      option match {
        case Some(value) => BigInt(value)
        case None => BigInt(0, true)
      }
    }

  }

  case class BigInt(v: Int, isNull: Boolean = false) extends Product with IterableOnce[Int] {

    def asOption: Option[Int] = if (isNull) {
      None
    } else {
      Some(v)
    }

    override def iterator: Iterator[Int] = asOption.iterator
  }

  type Array[A] = scala.List[A]
}

trait jsInterface {

  class native extends scala.annotation.Annotation

  def native[R]: R

  trait Object extends Product with OkResponse {
    override def canEqual(that: Any): Boolean = true
  }

  class JSGlobal(val s: String) extends scala.annotation.Annotation


}

