package Exercise2

import org.apache.spark.sql.SparkSession
import com.exercise2.BattleGraph
import com.exercise2.skills.{Skill, Move}
import com.exercise2.monsters.{Monster, Bestiary}
import scala.collection.mutable
import org.apache.spark.rdd.RDD

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
  import sqlContext.implicits._

  //
  val graph = Battles.battle1()
  println("start")
  val mg = graph.vertices.map{case (id, monster) => {
    monster.set("x", monster.getAsInt("x") + 1)
    (id, monster.get("name"), monster.get("x"))
  }}
  println("stop")
  mg.foreach(m => println(m._1, m._2, m._3))

}

object Battles {
  def battle1():BattleGraph = {
    val graph = new BattleGraph()

    for (i <- 0 until 100) {
      val m = new Monster(Bestiary.Solar)
      m.set("team", 1)
      m.set("x", 0)
      m.set("y", 0)
      m.set("z", 0)
      graph.add(m)
    }

    return graph
  }
}
