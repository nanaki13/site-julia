package bon.jo

import java.sql.{Connection, DriverManager, ResultSet}

import scala.collection.immutable

object Reprise extends App {


  (new ConProcess(DriverManager.getConnection("jdbc:sqlite:./julia.db"))).close()
}

