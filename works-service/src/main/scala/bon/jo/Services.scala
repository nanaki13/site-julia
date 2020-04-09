package bon.jo

import bon.jo.juliasite.model.Schema

import scala.collection.compat.Factory
import scala.collection.mutable

object Services {

  implicit val d: Factory[Int, List[Int]] = new Factory[Int, List[Int]]() {
    override def fromSpecific(it: IterableOnce[Int]): List[Int] = it.iterator.to(List)

    override def newBuilder: mutable.Builder[Int, List[Int]] = new mutable.ListBuffer[Int]()
  }

  def themeMapping(themeDB: Schema.Themes): RawImpl.ItemRawExport = {
    // (id, name,idThemeParent,x,y, final_theme)
    RawImpl.
      ItemRawExport(themeDB._1, themeDB._2, "", js.BigInt.Null, js.BigInt(themeDB._3), js.BigInt(themeDB._4), js.BigInt(themeDB._5))
  }

  def themeWithImage(themeDB: Schema.Themes, imgDb: Option[Schema.ImagesWithoutData]): RawImpl.ItemRawExport = {
    themeMapping(themeDB).copy(image = imgDb.map(_._1).map(js.BigInt(_)).getOrElse(js.BigInt.Null))
  }

  def imageMapping(imgDb: Schema.ImagesWithoutData): Int = {
    imgDb._1
  }



  def Type(boolean: Boolean): String = {
    if (boolean) {
      "page"
    } else {
      "subMenu"
    }
  }

  def Type(txt: String): Boolean = {
    txt match {
      case "page" => true

      case _ => false

    }
  }










}



