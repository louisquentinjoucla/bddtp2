package Exercise2

import com.exercise2.BattleGraph
import com.exercise2.monsters.{Bestiary, Monster}
import org.apache.spark.sql.SparkSession

//-------------------------------------------------------------------------------------------
//Main
object Exercise2 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Application
  println("hello world (ex2)")

  val sqlContext = spark.sqlContext

  //
  val graph = Battles.battle1()
  //graph.print()
  graph.next()
  graph.print()

}

object Battles {
  def battle1():BattleGraph = {
    val graph = new BattleGraph()

    for (i <- 0 until 1) {
      val m = new Monster(Bestiary.Solar)
      m.set("team", 1)
      m.set("x", 0)
      m.set("y", 0)
      m.set("z", 0)
      m.setActions(Seq((1, "move"), (2, "move")))
      graph.add(m)
    }

    for (i <- 0 until 10) {
      val m = new Monster(Bestiary.OrcBarbarian)
      m.set("team", 2)
      m.set("x", 0)
      m.set("y", 0)
      m.set("z", 0)
      graph.add(m)
    }

    graph.connect()

    return graph
  }
}
