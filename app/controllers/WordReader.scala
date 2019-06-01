package controllers

import java.io.{Reader, StringReader}

class WordReader(val reader: Reader) {

  val blank = Set(' ', '\t', '\n',',')

  var lastRead: Int = null.asInstanceOf[Int]
  var lastCharRead:  Char = null.asInstanceOf[Char]
  var valdidStartExpresion = () => lastCharRead != '"'
  var valdidEndExpresion = (c: Char) =>c != '"'
  def readChar() = {
    lastRead = reader.read()
    lastCharRead = lastRead.asInstanceOf[Char]
  }
  def readWhile(filter: Char => Boolean)(implicit stringBuilder: StringBuilder =null) = {

    readChar()
    while ( lastRead!= -1  && filter(lastCharRead) ) {
      if (stringBuilder != null){ stringBuilder.append(lastCharRead)}
      readChar()
    }
  }

  def readWord(): String = {
    implicit val bl = new StringBuilder
    bl.append(lastCharRead)
    val filter = if(valdidStartExpresion()){
       (c : Char) => !blank.contains(c)
    }else{
      valdidEndExpresion
    }

    readWhile(filter)
    if(!blank.contains(lastCharRead) && lastRead!= -1){
      bl.append(lastCharRead)
    }
    return bl.toString()
  }

  def skeepBlank(): Unit = {
    readWhile((c) => blank.contains(c))
  }

  def wordsIterator(): Iterator[String] = {
    new Iterator[String] {
      override def hasNext: Boolean = lastRead != -1

      override def next(): String = {
        skeepBlank()
        readWord()
      }
    }
  }

}
object WordReader extends App {
  def apply(reader: String): WordReader = new WordReader(
    new StringReader(reader)
  )


  val exp =  RootExpression("(dsf ddf  df   d sdfs) ( (qsdqsd ) ans (sdsdfsdf)) and (zefze ) ")
  println(exp.parse().mkString(" -- "))
}

trait ExpresionCont{
  var childs : List[ChildExpression] = List()
}
trait Expression{
    def haveChild :Boolean
    def isRoot: Boolean = false
    var value : String
    def parse(): List[String]
}
object RootExpression{
  def apply(valuep : String): RootExpression= {
      new RootExpression {
        override var value: String = valuep

        override def parse():  List[String] = {
          ParethesisExpresionReader(value).wordsIterator().toList
        }
      }

  }
}

trait RootExpression extends Expression with ExpresionCont {
  override def isRoot: Boolean = true
  override def haveChild: Boolean = true

}
trait ChildExpression extends Expression with ExpresionCont{

}

class ParethesisExpresionReader(override val reader: Reader) extends WordReader(reader=reader){
  valdidStartExpresion = () => {
    if(lastCharRead =='('){
      cnt+=1
    }else if(lastCharRead == ')'){
      cnt-=1
    }
    print(cnt)
    lastCharRead != '('}
  this.valdidEndExpresion = (c: Char) => {
    if(lastCharRead =='('){
      cnt+=1
    }else if(lastCharRead == ')'){
      cnt-=1
    }
    print(cnt)
    c != ')' && cnt!=0}
  var cnt = 0
  override def readWord(): String = {
    implicit val bl = new StringBuilder
    bl.append(lastCharRead)
    cnt = 0
    val filter = if(valdidStartExpresion()){
      (c : Char) => !blank.contains(c)
    }else{

      valdidEndExpresion
    }
    readWhile(filter)
    if(!blank.contains(lastCharRead) && lastRead!= -1){
      bl.append(lastCharRead)
    }
    return bl.toString()
  }

}

object ParethesisExpresionReader{
  def apply(reader: String): ParethesisExpresionReader = new ParethesisExpresionReader(
    new StringReader(reader)
  )
}