package bon.jo

import scala.reflect.runtime.universe._
import scala.collection.immutable
import scala.io.StdIn
case class Column(name:String,confName : String)
case class Table( size : Int,var columns : Seq[Column],tot : String)

trait DescriptorInput {
  val GetClass= "getClass"



  def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList



}
object Test extends App with  DescriptorInput{
  val ru = scala.reflect.runtime.universe
  val rm = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
  val tavles: immutable.Seq[Table] =

    for{table <-1 to 10
        t = Table(12, Nil,"")
        column <-1 to 10
  }yield {
    t.columns = Column(s"c $table-$column",s"c conf-$table-$column") +: t.columns
     t
  }
  val  d = classAccessors[Table]
  println(d)
  val instanceMirror = rm.reflect(Table(12,Nil,""))
// println(  instanceMirror.reflectMethod(d(1)).apply())
//  println(  instanceMirror.reflectMethod(d(1)).apply(1))
//  println(  instanceMirror.reflectField(d(1)).set(1))
//  println(  instanceMirror.reflectField(d(1)).get)
  for( e <- d){
    if( e.returnType == typeOf[String]){
     // val value = StdIn.readLine(s"Enter value for ${e.name}")
      //instanceMirror.reflectField(e).set(value)
    }else if(e.returnType <:< typeOf[Seq[_]]){
     println( e.returnType.typeArgs(0))
      val inCol = e.returnType.typeArgs(0)
      print(inCol.members.collect {
        case m: MethodSymbol if m.isCaseAccessor => m
      }.toList)
//     println( classAccessors[inCol.])
    }


  }
   println(instanceMirror.instance)


}

case class User(name : String,age : String)