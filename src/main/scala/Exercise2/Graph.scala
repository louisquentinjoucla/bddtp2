package com.exercise2
import com.exercise2.monsters.Monster
import com.exercise2.skills.Skill
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SparkSession

class BattleGraph() extends Serializable {

  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._


  var vertices = Seq[(Int, Monster, List[(Int, Int)])]().toDS()

  var turn:Int = 0
  var cid:Int = 0

  def add(v:Monster):Unit = {
    v.set("id", cid)
    val vertex = Seq[(Int, Monster, List[(Int, Int)])]((cid, v, List[(Int, Int)]())).toDS()
    vertices = vertices.union(vertex)
    cid += 1
  }

  
  def connect():Unit = {




    /*val x = vertices.map(v => (v._1, v._2.getAsInt("team"))).collect()

    vertices = vertices.map(va =>
      (va._1, va._2, x.map(vb => (vb._1, if (vb._2 == va._2.getAsInt("team")) 0 else 1)).toList)
    )*/

  }


  def next():Unit = {
    turn += 1


    val monsters = spark.sparkContext.broadcast(vertices.map{case (id, monster, edges) => (id, monster)}.collect())


    vertices.flatMap{case (id, monster, edges) => {
      val actions = monster.getActions()
      actions.map{case (target, skill) => {
        (id, Skill.execute(monster, skill, monsters.value(target)._2))
      }}
    }}.foreach(x => println(x))



  }
    
  def print():Unit = {
    println("==========================================")
    println(s"Turn ${turn}")
    vertices.show(false)
  }

}

