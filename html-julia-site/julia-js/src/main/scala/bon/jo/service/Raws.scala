package bon.jo.service

import scala.scalajs.js

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
  trait ImageRawExport extends WithId {
    val id: Int
    val link: String
    val base:String
  }


  @js.native
  trait ItemRawExport extends WithId {
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
  trait OeuvreRawExport extends WithId {
    val id: Int
    val image: Int
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


