package Exercise2

import com.exercise2.BattleGraph
import com.exercise2.monsters.Bestiary
import org.apache.spark.sql.SparkSession
import com.exercise2.WebServices

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
  //WebServices.start()

  //
  val graph = Battles.battle1()
  for (i <- 0 until 20) {
    graph.next()
    graph.print()
  }
  


}

object Battles {
  def battle1():BattleGraph = {
    val graph = new BattleGraph()

    for (i <- 0 until 1) {
      val m = Bestiary.Solar()
      m.set("team", 1)
      m.set("x", 0)
      m.set("y", 0)
      m.set("z", 0)
      graph.add(m)
    }

  /*  for (i <- 0 until 4) {
      val m = Bestiary.OrcBarbarian()
      m.set("team", 2)
      m.set("x", 130)
      m.set("y", -20 + i *15)
      m.set("z", 0)
      graph.add(m)
    }

    for (i <- 0 until 9) {
      val m = Bestiary.WorgRider()
      m.set("team", 2)
      m.set("x", 110)
      m.set("y", -50 + i *10)
      m.set("z", 0)
      graph.add(m)
    }*/

    for (i <- 0 until 2) {
      val m = Bestiary.WarLord()
      m.set("team", 2)
      m.set("x", 150)
      m.set("y", 0)
      m.set("z", 0)
      graph.add(m)
    }

    return graph
  }
}

//Solar (0) moves towards War Lord (1) | 0;0;0 -> 50;0;0
//War Lord (1) moves towards Solar (0) | 150;0;0 -> 300;0;0