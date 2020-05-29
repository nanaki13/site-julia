package bon.jo.view

import bon.jo.view.Expr.Expression

object Expr{
  sealed trait Expression{
    def ecrit(b : StringBuilder) : Unit
    def str : String
  }
  trait CapsuleExpression extends Expression{
    def left: Char = str(0)
    def right: Char = str(1)
    def inside : Expression
    def ecrit(b : StringBuilder) ={ b.append(left) ;inside.ecrit(b) ;b.append(right) }
  }
  class  StringExpression(s : String) extends Expression{
    def ecrit(b : StringBuilder) = b.append(s)
    val str: String = s
  }
  trait Op  extends Expression{
    def left : Expression
    def right : Expression
    def op = str
    override def ecrit(b: StringBuilder): Unit = {left.ecrit(b);b.append(str);right.ecrit(b) }
  }
  object Op{
    case class AND(left : Expression, right: Expression) extends StringExpression("AND") with Op
    case class OR(left : Expression, right: Expression) extends StringExpression("OR") with Op
    case class +(left : Expression, right: Expression) extends StringExpression("+") with Op

  }
}

trait Resolver[E <: Expression,R]{
  def resolve(e: E):R
}
trait MapperResult[R,E <:Expression]{
  def map(r: R):E
}