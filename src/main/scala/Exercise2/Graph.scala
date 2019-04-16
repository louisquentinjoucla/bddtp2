package com.exercise2
import com.exercise2.monsters.Monster
import com.exercise2.skills.Skill
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{collect_list, concat_ws, _}

//-------------------------------------------------------------------------------------------
//Graph representing battle

class BattleGraph() extends Serializable {

  val debug = true

  //Spark session
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._

  //Graph
  var vertices = Seq[(Int, Monster)]().toDS()
  var edges = Seq[(Int, Int, Int)]().toDS()

  //Battle data
  var turn:Int = 0
  var cid:Int = 0

  //Add a new monster to battle
  def add(v:Monster):Unit = {
    val vertex = Seq[(Int, Monster)]((cid, v)).toDS()
    vertices = vertices.union(vertex)
    cid += 1
  }

  //Connect monsters together
  def connect():Unit = {
    val v = vertices.map(va => (va._1, va._2.get("team"))).collect()
    edges = vertices.flatMap{case (ida, va) => {
      val ta = va.get("team")
      v.map{case (idb, tb) => (ida, if (ta == tb) 0 else 1, idb) }
    }}
  }

  //Compute next turn
  def next():Unit = {
    turn += 1
    if (debug) println(s"== Turn ${"%4d".format(turn)} ========================================")
    val monsters = spark.sparkContext.broadcast(vertices.map{case (id, monster) => (id, monster)}.collect())
    vertices = vertices
      //Compute differences depending on each individual monster's actions
      .flatMap{case (id, monster) => {
        val computed = Seq((id, "t", 1)) ++ monster.actions.flatMap{case (target, skill) => Skill.execute(monster, skill, monsters.value(target)._2).map(d => (id, d._1, d._2)) }
        computed
      }}
      //Merge differences
      .groupBy("_1", "_2")
      .agg(sum("_3").alias("_3"))
      .groupBy("_1")
      .agg(collect_list(concat_ws("_", array("_2", "_3"))).alias("_d"))
      .as[(Int, Seq[String])]
      //Apply differences
      .map{case (id, diffs) => {
        val m = monsters.value(id)._2
        m.actions = Seq()
        diffs
          .map(diff => { val p = diff.split("_") ; (p(0), p(1).toInt)})
          .foreach{case (k, v) => {
            if ((debug)&&(!k.equals("t"))) println(s"${m.name} (${id}) : ${k} ${if (v < 0) v else "+"+v} (${m.get(k)} -> ${m.get(k) + v})")
            m.set(k, m.get(k) + v)
          }}
        (id, m)
      }}
  }

  //Print current state
  def print():Unit = {
    vertices.show(false)
  }

}

