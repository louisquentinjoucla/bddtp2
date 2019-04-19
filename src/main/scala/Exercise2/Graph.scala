package com.exercise2
import com.exercise2.monsters.Monster
import com.exercise2.skills.Skill
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions.{array, collect_list, concat_ws, sum}

import scala.reflect.ClassTag
import scala.collection.JavaConverters._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe._
import io.circe.generic.semiauto._
import com.exercise2.WebServices
import org.apache.spark.broadcast.Broadcast
import scala.math._

//-------------------------------------------------------------------------------------------
//Graph representing battle

class BattleGraph() extends Serializable {

  //Spark session
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._

  //Graph
  var vertices = Seq[(Int, Monster)]().toDS()
  var edges = Seq[(Int, Int, Int, Int)]().toDS()

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
    val monsters = spark.sparkContext.broadcast(vertices.collect())
    val ids = vertices.map{case (id, monster) => (id)}.collect()
    var links = Seq[(Int, Int, Int, Int)]()
    ids.foreach(ida => {
      ids.foreach(idb => {
        val a = monsters.value(ida)._2
        val b = monsters.value(idb)._2
        val team = if (a.get("team") == b.get("team")) 0 else 1
        val distance = sqrt(pow(b.get("x") - a.get("x"), 2) + pow(b.get("y") - a.get("y"), 2) + pow(b.get("z") - a.get("z"), 2))    
        //Ennemies doesn't need to be connected each other
        if (!((team == 0)&&(b.get("team") > 1))) {
        //If opponent, can move towards
          if (team == 1)
            links = links ++ Seq((ida, team, 0, idb))
          //Melee
          if (distance < 10)
            links = links ++ Seq((ida, team, 1, idb))
          //Ranged
          if (distance < 100)
            links = links ++ Seq((ida, team, 2, idb))
        }
      })
    })

    edges = links.toDS()
  }


  //Compute next turn
  def next():Unit = {
    turn += 1
    println(s"== Turn ${"%4d".format(turn)} ========================================")
    val monsters = spark.sparkContext.broadcast(vertices.collect())
    vertices = vertices
      //Compute actions decided by each monster
      .map{case (id, monster) => {
        monster/// 
        (id, monster)
      }}
      //Compute differences depending on each individual monster's actions
      .flatMap{case (id, monster) => {
        val computed = Seq((id, "hp", monster.get("regen"))) ++ monster.actions.flatMap{case (target, skill) => Skill.execute(id, monster, skill, target, monsters.value(target)._2)}
        computed
      }}
      //Merge differences
      .groupBy("_1", "_2")
      .agg(sum("_3").alias("_3"))
      .groupBy("_1")
      .agg(collect_list(array("_2", "_3")).alias("_d"))
      .as[(Int, Seq[Seq[String]])]
      //Apply differences
      .map{case (id, diffs) => {
        val m = monsters.value(id)._2
        m.actions = Seq()
        diffs
          .map(diff => { (diff(0), diff(1).toInt)})
          .foreach{case (k, v) => m.set(k, m.get(k) + v)}
        (id, m)
      }}
      //Filter monsters by hp
      .filter(m => m._2.get("hp") > 0)
  }

  //Print current state
  def print():Unit = {
    vertices.show(false)
  }

  //Send to websockets
  def send():Unit = {
    import spark.implicits._
    val json = vertices.map(r => r._2).collectAsList().asScala.toList.asJson.noSpaces
    WebServices.send(json)
    println(s"sent data from turn ${turn}")
  }

}

