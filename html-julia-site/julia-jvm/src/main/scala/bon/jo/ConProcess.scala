package bon.jo

import java.sql.{Connection, ResultSet}

import scala.collection.immutable

class ConProcess(val connection: Connection) {
  def close() = {
    r.close()
    statment.close()
    connection.close()
  }


  implicit class ResultProcess(resultSet: ResultSet) {
    def readForMd(): (immutable.Seq[(Int, String)], List[List[Any]]) = {
      var ret = List[List[Any]]()
      val md = resultSet.getMetaData
      val cnt = md.getColumnCount
      val columnByIndex: immutable.Seq[(Int, String)] = for {
        cbt <- 1 to cnt
        name = md.getColumnName(cnt)
      } yield (cbt, name)
      println(columnByIndex)
      while (resultSet.next()) {
        ret = ret :+ (for {
          _ <- 1 to cnt
          obj = resultSet.getObject(cnt)
        } yield {
          println(obj)
          obj
        }).toList
      }
      (columnByIndex,ret)
    }

  }



  val statment = connection.createStatement()
  val r = statment.executeQuery("select * from image")
  println(r.readForMd())




}


