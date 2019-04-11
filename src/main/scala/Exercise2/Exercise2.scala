package Exercise2

import org.apache.spark.sql.SparkSession
import com.exercise2.BattleGraph
import com.exercise2.skills.{Skill, Move}
import com.exercise2.monsters.{Monster, Solar, WorgRider, OrcBarbarian, WarLord}

//-------------------------------------------------------------------------------------------
//Main
object Exercise2 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .config("spark.testing.memory", "2147480000")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Application
  println("hello world (ex2)")

  val graph = Battles.battle1()
  graph.print()
  graph.next()
  graph.print()

}

object Battles {
  def battle1():BattleGraph = {
    val graph = new BattleGraph()
    //Create vertices
    for (i <- 0 until 1) {
      val m = new Solar()
      m.parameters("team") = 1
      m.setPosition(0, 0, 0)
      graph.add(m)
    }
    for (i <- 0 until 9) {
      val m = new WorgRider()
      m.parameters("team") = 2
      m.setPosition(i, 0, 0)
      graph.add(m)
    }
    for (i <- 0 until 4){
      val m = new OrcBarbarian()
      m.parameters("team") = 2
      m.setPosition(1, i, 0)
      graph.add(m)
    }
    for (i <- 0 until 1){
      val m = new WarLord()
      m.parameters("team") = 2
      m.setPosition(2, 2, 0)
      graph.add(m)
    }

    //Create edges
    val vertices = graph.vertices.collect()

    vertices.foreach(a => {
      vertices.foreach(b => {
        graph.connect(a, a.skills, b)
      })
    })

    return graph
  }
}

